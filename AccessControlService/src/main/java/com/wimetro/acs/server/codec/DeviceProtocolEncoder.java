package com.wimetro.acs.server.codec;

import com.wimetro.acs.common.AcsResponseMessage;
import com.wimetro.acs.common.MessageHeader;
import com.wimetro.acs.common.OperationType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @title: ProtocolEncoder
 * @author: Ellie
 * @date: 2022/02/10 16:42
 * @description:
 **/
@Slf4j
public class DeviceProtocolEncoder extends MessageToMessageEncoder<AcsResponseMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, AcsResponseMessage responseMessage, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();
        responseMessage.encode(buffer);

        // 控制心跳数据打印
        MessageHeader header = responseMessage.getMessageHeader();
        if (header.getMsgType() != OperationType.HEART.getReOpCode()) {
            log.info("[encode]{} -> {}", buffer.toString(StandardCharsets.UTF_8), header.getTargetIp());
        }
        out.add(buffer);
    }
}
