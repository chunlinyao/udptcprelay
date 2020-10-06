package com.github.chunlinyao.udptcprelay.server;

import com.github.chunlinyao.udptcprelay.codec.MyFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPRelay {

    private final Server server;
    private final EventLoopGroup group;
    private final String remoteHost;
    private final int remotePort;
    private final int sessionId;
    private ChannelFuture channelFuture;
    private Channel outboundChannel;

    UDPRelay(Server server, EventLoopGroup group, String remoteHost, int remotePort, int sessionId) {
        this.server = server;
        this.group = group;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.sessionId = sessionId;
    }

    void start() {

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new UDPRelayInitializer(this))
                .bind(0);
        channelFuture = b.connect(remoteHost, remotePort);
        outboundChannel = channelFuture.channel();
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void udpToTcp(DatagramPacket msg) {
        server.udpToTcp(new MyFrame(sessionId, msg.content().retainedDuplicate()));
    }

    public void tcpToUdp(MyFrame msg) {
        outboundChannel.write(msg.content());
    }

    public void invalidate() {
        server.invalidate(sessionId);
    }

    public void udpToTcpFlush() {
        server.udpToTcpFlush();
    }

    public void nextActiveTcpChannel() {
        server.nextActiveTcpChannel();
    }
    public void tcpToUdpFlush() {
        outboundChannel.flush();
    }
}
