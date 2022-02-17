package com.wimetro.acs.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldPrepender;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * @title: FrameEncoder
 * @author: Ellie
 * @date: 2022/02/10 16:44
 * @description:
 **/
public class FrameEncoder extends LengthFieldPrepender {
    private static final int lengthFieldOffset = 5;
    private static final int lengthFieldLength = 4;

    public FrameEncoder() {
        super(4, -9);

    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int length = msg.readableBytes() + lengthFieldLength;

        checkPositiveOrZero(length, "length");

        // 长度字符串，高位补0
        String format = "%0" + lengthFieldLength + "d";
        String lengthStr = String.format(format, length);
        ByteBuf lengthBuf = ctx.alloc().buffer(lengthFieldLength).writeBytes(lengthStr.getBytes(StandardCharsets.UTF_8));

        ByteBuf msgHeadBuf = msg.readBytes(lengthFieldOffset);
        out.add(msgHeadBuf);
        out.add(lengthBuf);
        out.add(msg.retain());
    }
}
