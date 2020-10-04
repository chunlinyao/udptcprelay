package com.github.chunlinyao.udptcprelay.client;

import com.github.chunlinyao.udptcprelay.codec.MyFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TCPRelay {

    private final EventLoopGroup group;
    private final String remoteHost;
    private final int remotePort;
    private final Client client;
    private Bootstrap bs;
    private ChannelFuture channelFuture;
    private Channel outboundChannel;

    public TCPRelay(Client client, EventLoopGroup workerGroup, String remoteHost, int remotePort) {
        this.client = client;
        this.group = workerGroup;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void start() throws InterruptedException {
        // Start the connection attempt.
        bs = new Bootstrap();
        bs.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.IP_TOS, 152)
                .handler(new TCPRelayInitializer(this));
        connect();
    }

    void connect() {
        channelFuture = bs.connect(remoteHost, remotePort);
        outboundChannel = channelFuture.channel();
    }


    public void udpToTcp(MyFrame myFrame) {
        outboundChannel.writeAndFlush(myFrame);
    }

    public void tcpToUdp(MyFrame msg) {
        client.tcpToUdp(msg);
    }

    public boolean isActive() {
        return outboundChannel.isActive();
    }
}
