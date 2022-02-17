package com.wimetro.acs.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @title: HeartbeatHandler
 * @author: Ellie
 * @date: 2022/02/15 16:22
 * @description: 空闲检测 -- 心跳及连接维护
 **/
@Slf4j
public class KeepaliveHandler extends ChannelInboundHandlerAdapter {
    private int readIdleTimes = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {

            // TODO:发送心跳包
//            log.info("write idle happen. so need to send keepalive to keep connection not closed by server");
//            KeepaliveOperation keepaliveOperation = new KeepaliveOperation();
//            RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(), keepaliveOperation);
//            ctx.writeAndFlush(requestMessage);

            // 发送失败：关闭通道示例


        } else if (evt == IdleStateEvent.READER_IDLE_STATE_EVENT) {
            // 发送心跳包
            readIdleTimes++;
        }

        // 重试逻辑
        if (readIdleTimes > 3) {
            System.out.println(" [server]读空闲超过3次，关闭连接，释放更多资源");
            ctx.channel().writeAndFlush("idle close");
            ctx.channel().close(); // 手动断开连接
        }

        super.userEventTriggered(ctx, evt);
    }
}
