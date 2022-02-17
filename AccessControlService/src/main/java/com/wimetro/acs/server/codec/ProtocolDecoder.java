package com.wimetro.acs.server.codec;

import com.wimetro.acs.common.AcsRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @title: ProtocolDecoder
 * @author: Ellie
 * @date: 2022/02/10 11:25
 * @description:
 **/
@Slf4j
public class ProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {

//        String contextStr = byteBuf.toString(CharsetUtil.UTF_8);
//        log.info("接收消息：" + contextStr);

        AcsRequestMessage acsRequestMessage = new AcsRequestMessage();
        acsRequestMessage.decode(byteBuf);

        out.add(acsRequestMessage);
    }
}
