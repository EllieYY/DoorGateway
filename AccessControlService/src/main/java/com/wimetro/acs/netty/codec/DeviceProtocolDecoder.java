package com.wimetro.acs.netty.codec;

import com.wimetro.acs.common.AcsRequestMessage;
import com.wimetro.acs.netty.runner.ChannelManager;
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
public class DeviceProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {
    private String webKey;

    public DeviceProtocolDecoder(String webKey) {
        this.webKey = webKey;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        String contextStr = byteBuf.toString(CharsetUtil.UTF_8);
//        log.info("[device报文]{}", contextStr);

        // targetIp默认从配置中获取--页面端的ip:端口
        String channelKey = ChannelManager.getChannelKey(ctx.channel());
        String targetKey = webKey;

        AcsRequestMessage acsRequestMessage = new AcsRequestMessage();
        if (acsRequestMessage.decode(byteBuf, channelKey, targetKey)) {
            out.add(acsRequestMessage);
        } else {
            log.error("[无法解析报文]{} - [{}]", channelKey, contextStr);
        }
    }
}
