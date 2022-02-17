package com.wimetro.acs.server.codec;

import com.wimetro.acs.common.AcsResponseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @title: ProtocolEncoder
 * @author: Ellie
 * @date: 2022/02/10 16:42
 * @description:
 **/
@Slf4j
public class ProtocolEncoder extends MessageToMessageEncoder<AcsResponseMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, AcsResponseMessage responseMessage, List<Object> out) throws Exception {
//        log.info("ProtocolEncoder");

        ByteBuf buffer = ctx.alloc().buffer();
        responseMessage.encode(buffer);

        // TODO:
        String contextStr = buffer.toString(CharsetUtil.UTF_8);
        log.info("发送消息：" + contextStr);

        out.add(buffer);
    }
}
