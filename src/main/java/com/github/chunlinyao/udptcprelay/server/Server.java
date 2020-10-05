/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.chunlinyao.udptcprelay.server;

import com.github.chunlinyao.udptcprelay.codec.MyFrame;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Server implements RemovalListener<Integer, UDPRelay> {


    static final String REMOTE_HOST = System.getProperty("remoteHost", "127.0.0.1");
    static final int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "7696"));
    private static final int PORT = Integer.parseInt(System.getProperty("port", "7666"));
    private final Cache<Integer, UDPRelay> sessionRelayCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(this).build();
    private TCPServer tcpServer;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public static void main(String[] args) throws Exception {
        new Server().start();
    }

    public void start() throws Exception {

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            tcpServer = new TCPServer(this, bossGroup, workerGroup, PORT);
            tcpServer.start();
        } finally {
            if (tcpServer != null) {
                tcpServer.getChannelFuture().sync().channel().closeFuture().sync();
            }
            for (UDPRelay udpRelay : sessionRelayCache.asMap().values()) {
                udpRelay.getChannelFuture().sync().channel().closeFuture().sync();
                udpRelay.getChannelFuture().sync().channel().close();
            }
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void tcpToUdp(MyFrame msg) {
        final int sessionId = msg.getSessionId();
        try {
            UDPRelay udpRelay = sessionRelayCache.get(sessionId, () -> {
                UDPRelay tmp = new UDPRelay(Server.this, workerGroup, REMOTE_HOST, REMOTE_PORT, sessionId);
                tmp.start();
                return tmp;
            });
            //FIXME why lost first packet when create new UDPRelay.
            udpRelay.tcpToUdp(msg.retain());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            msg.release();
        }
    }

    public void udpToTcp(MyFrame frame) {
        tcpServer.udpToTcp(frame);
    }

    @Override
    public void onRemoval(RemovalNotification<Integer, UDPRelay> notification) {
        Channel channel = notification.getValue().getChannelFuture().channel();
        if (channel.isActive()) {
            channel.close();
        }
    }

    public void invalidate(int sessionId) {
        sessionRelayCache.invalidate(sessionId);
    }
}
