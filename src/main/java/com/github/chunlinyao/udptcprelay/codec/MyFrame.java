package com.github.chunlinyao.udptcprelay.codec;

import io.netty.buffer.ByteBuf;

public class MyFrame {

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

    public ByteBuf getData() {
        return data;
    }
}

