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
 * @title: WebProtocolDecoder
 * @author: Ellie
 * @date: 2022/02/10 11:25
 * @description:
 **/
@Slf4j
public class WebProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {
    private int port;

    public WebProtocolDecoder(int port) {
        this.port = port;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        String contextStr = byteBuf.toString(CharsetUtil.UTF_8);
//        log.info("[web报文]{}", contextStr);

        ByteBuf msgTypeBuf = byteBuf.readBytes(15);
        byteBuf.skipBytes(1);    // 分隔符

        String targetIp = msgTypeBuf.toString(CharsetUtil.UTF_8);
        String targetKey = targetIp.trim() + ":" + port;
        String channelKey = ChannelManager.getChannelKey(ctx.channel());

        AcsRequestMessage acsRequestMessage = new AcsRequestMessage();
        if (acsRequestMessage.decode(byteBuf, channelKey, targetKey)) {
            out.add(acsRequestMessage);
        } else {
            log.error("[无法解析报文]{} - {}", channelKey, contextStr);
        }
    }
}
