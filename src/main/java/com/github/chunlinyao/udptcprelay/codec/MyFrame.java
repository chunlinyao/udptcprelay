package com.github.chunlinyao.udptcprelay.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

public class MyFrame implements ByteBufHolder {

    public static final int HEARTBEAT_REQ_SESSIONID = 65534;
    public static final int HEARTBEAT_RESP_SESSIONID = HEARTBEAT_REQ_SESSIONID + 1;

    private final int sessionId;
    private final ByteBuf data;

    public MyFrame(int sessionId, ByteBuf slice) {
        this.sessionId = sessionId;
        this.data = slice;
    }

    public int getSessionId() {
        return sessionId;
    }

    @Override
    public ByteBuf content() {
        return data;
    }


    @Override
    public MyFrame copy() {
        return replace(data.copy());
    }

    @Override
    public MyFrame duplicate() {
        return replace(data.duplicate());
    }

    @Override
    public MyFrame retainedDuplicate() {
        return replace(data.retainedDuplicate());
    }

    @Override
    public MyFrame replace(ByteBuf content) {
        return new MyFrame(sessionId, content);
    }

    @Override
    public int refCnt() {
        return data.refCnt();
    }

    @Override
    public MyFrame retain() {
        data.retain();
        return this;
    }

    @Override
    public MyFrame retain(int increment) {
        data.retain(increment);
        return this;
    }

    @Override
    public MyFrame touch() {
        data.touch();
        return this;
    }

    @Override
    public MyFrame touch(Object hint) {
        data.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return data.release();
    }

    @Override
    public boolean release(int decrement) {
        return data.release(decrement);
    }

    @Override
    public String toString() {
        return "MyFrame{" +
                "sessionId=" + sessionId +
                ", data=" + data +
                '}';
    }
}

