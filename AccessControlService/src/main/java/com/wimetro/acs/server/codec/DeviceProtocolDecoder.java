package com.wimetro.acs.server.codec;

import com.wimetro.acs.common.AcsRequestMessage;
import com.wimetro.acs.server.runner.ChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
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

//        String contextStr = byteBuf.toString(CharsetUtil.UTF_8);
//        log.info("DeviceProtocolDecoder接收消息：" + contextStr);

//        InetSocketAddress iNetAddress = (InetSocketAddress) ctx.channel().remoteAddress();
//        String channelIp = iNetAddress.getAddress().getHostAddress();
        String channelKey = ChannelManager.getChannelKey(ctx.channel());

        // targetIp默认从配置中获取--页面端的ip:端口
        String targetKey = webKey;

        AcsRequestMessage acsRequestMessage = new AcsRequestMessage();
        acsRequestMessage.decode(byteBuf, channelKey, targetKey);

        out.add(acsRequestMessage);
    }
}
