package com.wimetro.acs.server.codec;

import com.wimetro.acs.common.AcsRequestMessage;
import com.wimetro.acs.server.runner.ChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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

//        String contextStr = byteBuf.toString(CharsetUtil.UTF_8);
//        log.info("WebProtocolDecoder接收消息：" + contextStr);

        ByteBuf msgTypeBuf = byteBuf.readBytes(15);
        byteBuf.skipBytes(1);    // 分隔符

        String targetIp = msgTypeBuf.toString(CharsetUtil.UTF_8);
        String targetKey = targetIp.trim() + ":" + port;

//        InetSocketAddress iNetAddress = (InetSocketAddress) ctx.channel().remoteAddress();
//        String channelIp = iNetAddress.getAddress().getHostAddress();
        String channelKey = ChannelManager.getChannelKey(ctx.channel());

        AcsRequestMessage acsRequestMessage = new AcsRequestMessage();
        acsRequestMessage.decode(byteBuf, channelKey, targetKey);

        out.add(acsRequestMessage);
    }
}
