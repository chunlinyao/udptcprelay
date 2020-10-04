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
import com.github.chunlinyao.udptcprelay.common.RoundRobin;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.CopyOnWriteArrayList;

public final class TCPServer {


    private final Server server;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final int port;
    private final CopyOnWriteArrayList<ChannelHandlerContext> tcpChannels = new CopyOnWriteArrayList<>();
    private final RoundRobin<ChannelHandlerContext> roundRobin = new RoundRobin<>(tcpChannels, ChannelHandlerContext.class);
    private ChannelFuture channelFuture;

    public TCPServer(Server server, EventLoopGroup bossGroup, EventLoopGroup workerGroup, int port) {
        this.server = server;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.port = port;
    }

    public void start() throws Exception {

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new TCPServerInitializer(this))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.IP_TOS, 152);

        channelFuture = b.bind(port);
    }

    public void tcpToUdp(MyFrame msg) {
        server.tcpToUdp(msg);
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void udpToTcp(MyFrame myFrame) {
        ChannelHandlerContext ctx = nextActiveChannel();
        if (ctx != null) {
            ctx.writeAndFlush(myFrame);
        } else {
            ReferenceCountUtil.release(myFrame.getData());
        }
    }

    private ChannelHandlerContext nextActiveChannel() {
        ChannelHandlerContext tmp = roundRobin.get();
        ChannelHandlerContext first = tmp;
        while (tmp != null && tmp.channel().isActive() == false) {
            tmp = roundRobin.get();
        }
        if (tmp == first) {
            return null;
        }
        return tmp;
    }

    public void addChannel(ChannelHandlerContext ctx) {
        tcpChannels.add(ctx);
    }

    public void removeChannel(ChannelHandlerContext ctx) {
        tcpChannels.remove(ctx);
    }
}
