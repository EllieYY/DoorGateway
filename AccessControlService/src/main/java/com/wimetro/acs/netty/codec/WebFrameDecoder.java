package com.wimetro.acs.netty.codec;

import com.wimetro.acs.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

/**
 * @title: WebFrameDecoder
 * @author: Ellie
 * @date: 2022/02/10 10:45
 * @description:
 **/
public class WebFrameDecoder extends LengthFieldBasedFrameDecoder {

    public WebFrameDecoder() {
        super(Integer.MAX_VALUE, 21, 4, -25, 0);
    }


    @Override
    protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
        buf = buf.order(order);
        long frameLength;
        switch(length) {
            case 1:
                frameLength = (long)buf.getUnsignedByte(offset);
                break;
            case 2:
                frameLength = (long)buf.getUnsignedShort(offset);
                break;
            case 3:
                frameLength = (long)buf.getUnsignedMedium(offset);
                break;
            case 4:
                byte[] dst = new byte[length];
                buf.getBytes(offset, dst, 0, length);
                String lengthStr = new String(dst, 0, length);
                frameLength = Long.parseLong(lengthStr) + Constants.MSG_PREFIX_LENGTH;
//                frameLength = buf.getUnsignedInt(offset);
                break;
            case 5:
            case 6:
            case 7:
            default:
                throw new DecoderException("unsupported lengthFieldLength: " + length + " (expected: 1, 2, 3, 4, or 8)");
            case 8:
                frameLength = buf.getLong(offset);
        }

        return frameLength;
    }
}
