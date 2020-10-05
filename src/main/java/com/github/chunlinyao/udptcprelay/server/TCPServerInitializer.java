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

import com.github.chunlinyao.udptcprelay.codec.FrameDecoder;
import com.github.chunlinyao.udptcprelay.codec.FrameEncoder;
import com.github.chunlinyao.udptcprelay.common.HeartBeatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Creates a newly configured {@link ChannelPipeline} for a server-side channel.
 */
public class TCPServerInitializer extends ChannelInitializer<SocketChannel> {

    private final TCPServer tcpServer;

    public TCPServerInitializer(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        ch.config().setTrafficClass(152);

        // Add the number codec first,
        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(64 * 1024, 0, 2, 0, 2));
        pipeline.addLast(new FrameDecoder());
        pipeline.addLast(new LengthFieldPrepender(2));
        pipeline.addLast(new FrameEncoder());
        // and then business logic.
        // Please note we create a handler for every new channel
        // because it has stateful properties.
        pipeline.addLast(new IdleStateHandler(60, 0, 0));
        pipeline.addLast(new HeartBeatHandler());
        pipeline.addLast(new TCPServerHandler(tcpServer));
    }
}
