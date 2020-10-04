package com.github.chunlinyao.udptcprelay.client;

import com.github.chunlinyao.udptcprelay.codec.MyFrame;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

public class UDPServer implements RemovalListener<InetSocketAddress, Integer> {

    private final EventLoopGroup group;
    private final int port;
    private final Client client;
    private final Cache<InetSocketAddress, Integer> sessionIdMap = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(this).build();
    private final ConcurrentHashMap<Integer, InetSocketAddress> senderAddressMap = new ConcurrentHashMap<>();
    private final AtomicInteger nextSessionId = new AtomicInteger(0);
    private ChannelFuture channelFuture;
    private Channel outboundChannel;

    public UDPServer(Client client, EventLoopGroup workerGroup, int port) {
        this.client = client;
        this.group = workerGroup;
        this.port = port;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void start() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new UDPServerInitializer(this));

        this.channelFuture = b.bind(port);
        outboundChannel = this.channelFuture.channel();
    }

    public void udpToTcp(DatagramPacket packet) {
        InetSocketAddress key = packet.sender();
        try {
            int sessionId = sessionIdMap.get(key, () -> {
                int tmp = nextSessionId();
                senderAddressMap.putIfAbsent(tmp, key);
                return tmp;
            });
            client.udpToTcp(new MyFrame(sessionId, packet.content().retainedDuplicate()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private int nextSessionId() {
        return nextSessionId.getAndUpdate(new IntUnaryOperator() {
            @Override
            public int applyAsInt(int operand) {
                return operand + 1 % MyFrame.HEARTBEAT_REQ_SESSIONID;
            }
        });
    }

    public void tcpToUdp(MyFrame msg) {
        int sessionId = msg.getSessionId();
        InetSocketAddress sender = senderAddressMap.get(sessionId);
        if (sender != null) {
            outboundChannel.writeAndFlush(new DatagramPacket(msg.getData(), sender));
        }
    }

    @Override
    public void onRemoval(RemovalNotification<InetSocketAddress, Integer> notification) {
       senderAddressMap.remove(notification.getValue());
    }
}
