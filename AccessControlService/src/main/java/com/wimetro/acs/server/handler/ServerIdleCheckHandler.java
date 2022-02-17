package com.wimetro.acs.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @title: ServerIdleCheckHandler
 * @author: Ellie
 * @date: 2022/02/15 16:10
 * @description:
 **/
@Slf4j
public class ServerIdleCheckHandler extends IdleStateHandler {
    public ServerIdleCheckHandler() {
        super(30, 0, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
            log.info("idle check happen, so close the connection");
            // TODO:空闲情况的处理
//            ctx.close();
            return;
        }

        super.channelIdle(ctx, evt);
    }
}
