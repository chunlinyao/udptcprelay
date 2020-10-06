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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final UDPServer udpServer;
    private volatile ChannelHandlerContext udpContext;

    public UDPServerHandler(UDPServer server) {
        this.udpServer = server;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        InetSocketAddress sender = packet.sender();
        if (udpContext == null) {
            udpContext = ctx;
        }
        udpServer.udpToTcp(packet);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        udpServer.udpToTcpFlush();
        udpServer.nextActiveTcpRelay();
        super.channelReadComplete(ctx);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

}
