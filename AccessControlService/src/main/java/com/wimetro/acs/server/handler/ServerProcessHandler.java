package com.wimetro.acs.server.handler;

import com.wimetro.acs.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @title: ServerProcessHandler
 * @author: Ellie
 * @date: 2022/02/10 15:54
 * @description:
 **/
@Slf4j
public class ServerProcessHandler extends SimpleChannelInboundHandler<AcsRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AcsRequestMessage requestMessage) throws Exception {
        Operation operation = requestMessage.getMessageBody();
        OperationResult operationResult = operation.execute();

        // 消息头组装
        int opCode = requestMessage.getMessageHeader().getMsgType();
        int reCode = 0;
        switch (opCode) {
            case 1080:
                reCode = 80;
                break;
            case 1042:
                reCode = 70;
                break;
            case 1065:
                reCode = 67;
                break;
            default:
                reCode = 0;
                break;
        }
        AcsResponseMessage responseMessage = new AcsResponseMessage();
        MessageHeader header = new MessageHeader();
        header.setMsgType(reCode);
        responseMessage.setMessageHeader(header);
        responseMessage.setMessageBody(operationResult);

        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
            ctx.writeAndFlush(responseMessage);
        } else {
            log.error("not writable now, message dropped");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("{} -> [连接成功]", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("{} -> [断开连接]", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        log.error("{} -> [异常]原因：{}", ctx.channel().remoteAddress().toString(),
                cause.getMessage());
    }
}
