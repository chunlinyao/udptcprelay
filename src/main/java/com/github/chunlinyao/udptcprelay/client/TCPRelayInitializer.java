package com.github.chunlinyao.udptcprelay.client;

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

public class TCPRelayInitializer extends ChannelInitializer<SocketChannel> {
    private final TCPRelay relay;

    public TCPRelayInitializer(TCPRelay relay) {
        this.relay = relay;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
//        ch.config().setTrafficClass(152);
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
        pipeline.addLast(new TCPRelayHandler(relay));
    }
}
