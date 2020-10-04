package com.github.chunlinyao.udptcprelay.client;

import com.github.chunlinyao.udptcprelay.codec.MyFrame;
import com.github.chunlinyao.udptcprelay.common.RoundRobin;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.ArrayList;
import java.util.List;

public class Client {
    static final String REMOTE_HOST = System.getProperty("remoteHost", "127.0.0.1");
    static final int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "7666"));
    private static final int PORT = Integer.parseInt(System.getProperty("port", "7686"));
    private final List<TCPRelay> tcpRelays = new ArrayList<>();
    private final RoundRobin<TCPRelay> roundRobin = new RoundRobin<>(tcpRelays, TCPRelay.class);
    private UDPServer udpServer;

    public static void main(String[] args) throws Exception {
        new Client().start();
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            udpServer = new UDPServer(this, workerGroup, PORT);
            udpServer.start();
            for (int i = 0; i < 5; i++) {
                TCPRelay tcpRelay = new TCPRelay(this, workerGroup, REMOTE_HOST, REMOTE_PORT);
                tcpRelay.start();
                tcpRelays.add(tcpRelay);
            }
        } finally {
            if (udpServer != null) {
                udpServer.getChannelFuture().sync().channel().closeFuture().sync();
            }
            for (TCPRelay tcpRelay : tcpRelays) {
                tcpRelay.getChannelFuture().sync().channel().closeFuture().sync();
            }
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void udpToTcp(MyFrame myFrame) {
        TCPRelay tcpRelay = nextActiveTcpRelay();
        tcpRelay.udpToTcp(myFrame);
    }

    private TCPRelay nextActiveTcpRelay() {
        TCPRelay tmp = roundRobin.get();
        TCPRelay first = tmp;
        while (tmp != null && tmp.isActive() == false) {
            tmp = roundRobin.get();
            if (tmp == first) {
                return null;
            }
        }
        return tmp;
    }

    public void tcpToUdp(MyFrame msg) {
        udpServer.tcpToUdp(msg);
    }
}
