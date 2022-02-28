package com.wimetro.acs.server.codec;

import com.wimetro.acs.common.AcsResponseMessage;
import com.wimetro.acs.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @title: ProtocolEncoder
 * @author: Ellie
 * @date: 2022/02/10 16:42
 * @description:
 **/
@Slf4j
public class WebProtocolEncoder extends MessageToMessageEncoder<AcsResponseMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, AcsResponseMessage responseMessage, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();

        // 设备Ip拼接
        String deviceKey = responseMessage.getMessageHeader().getSourceIp();
        int spliterIndex = deviceKey.indexOf(":");
        String deviceIp = deviceKey.substring(0, spliterIndex);
        String ipString = StringUtil.padLeftSpaces(deviceIp, 15) + ";";
        buffer.writeBytes(ipString.getBytes(StandardCharsets.UTF_8));

        responseMessage.encode(buffer);
        out.add(buffer);
    }
}
