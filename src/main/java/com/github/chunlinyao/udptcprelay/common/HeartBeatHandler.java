package com.github.chunlinyao.udptcprelay.common;

import com.github.chunlinyao.udptcprelay.codec.MyFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.util.concurrent.atomic.AtomicInteger;


public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.ISO_8859_1));
    private final AtomicInteger reqRespDiff = new AtomicInteger(0);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MyFrame) {
            MyFrame frame = (MyFrame) msg;
            if (frame.getSessionId() == MyFrame.HEARTBEAT_RESP_SESSIONID) {
                reqRespDiff.set(0);
                return;
            } else if (frame.getSessionId() == MyFrame.HEARTBEAT_REQ_SESSIONID) {
                ctx.writeAndFlush(new MyFrame(MyFrame.HEARTBEAT_RESP_SESSIONID, HEARTBEAT_SEQUENCE.duplicate())).addListener(
                        ChannelFutureListener.CLOSE_ON_FAILURE);
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            int diff = this.reqRespDiff.getAndIncrement();
            if (diff > 5) {
                ctx.close();
                return;
            }
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
                ctx.writeAndFlush(new MyFrame(MyFrame.HEARTBEAT_REQ_SESSIONID, HEARTBEAT_SEQUENCE.duplicate())).addListener(
                        ChannelFutureListener.CLOSE_ON_FAILURE);

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
