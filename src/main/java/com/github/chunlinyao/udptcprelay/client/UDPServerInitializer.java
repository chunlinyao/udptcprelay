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
package com.github.chunlinyao.udptcprelay.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Creates a newly configured {@link ChannelPipeline} for a server-side channel.
 */
public class UDPServerInitializer extends ChannelInitializer<DatagramChannel> {


    private final UDPServer udpServer;

    public UDPServerInitializer(UDPServer udpServer) {
        this.udpServer = udpServer;
    }

    @Override
    public void initChannel(DatagramChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new UDPServerHandler(udpServer));
    }
}
