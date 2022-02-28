package com.wimetro.acs.server.handler;

import com.wimetro.acs.common.*;
import com.wimetro.acs.server.runner.ChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Objects;

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
        int reCode = requestMessage.getResponseOperationCode(opCode);

        // 设备注册
        if (opCode == OperationType.REGISTRY.getOpCode()) {
            ChannelManager.registry(ctx.channel());
        }

        String targetIp = requestMessage.getMessageHeader().getTargetIp();
        AcsResponseMessage responseMessage = new AcsResponseMessage();
        MessageHeader header = new MessageHeader();
        header.setMsgType(reCode);
        header.setSourceIp(requestMessage.getMessageHeader().getSourceIp());
        header.setTargetIp(targetIp);
        responseMessage.setMessageHeader(header);
        responseMessage.setMessageBody(operationResult);

        // 获取发送channel
        Channel targetChannel = ChannelManager.getChannelByIp(targetIp);
        if (Objects.isNull(targetChannel)) {
            log.error("发送目标{}未注册", targetIp);
            return;
        }

        writeToClient(responseMessage, targetChannel);
    }


    public void writeToClient(final AcsResponseMessage responseMessage, Channel channel) {
        if (channel.isActive() && channel.isWritable()) {
            try {
                channel.writeAndFlush(responseMessage).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            log.error(responseMessage.toString() + "发送失败");
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                log.error("调用通用writeToClient()异常：", e);
            }
        } else {
            log.error("not writable now, message dropped");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        Channel ch = ctx.channel();
        log.info("{} -> [连接成功]", ch.remoteAddress().toString().substring(1));
        ChannelManager.addChannelToGroup(ch);

        // 页面连接即有效
        InetSocketAddress serverSocket = (InetSocketAddress)ch.localAddress();
        int localPort = serverSocket.getPort();
        if (localPort == Constants.WEB_PORT) {
            ChannelManager.registry(ch);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        // 连接关系维护
        Channel ch = ctx.channel();
        ChannelManager.logout(ch);
        ChannelManager.removeChannelFromGroup(ch);
        ChannelManager.logout(ch);
        log.info("{} !- [断开连接]", ch.remoteAddress());

        // 连接状态通知
        InetSocketAddress serverSocket = (InetSocketAddress)ch.localAddress();
        int localPort = serverSocket.getPort();
        // 设备断开需要通知
        if (localPort == Constants.DEVICE_PORT) {
            //TODO: 客户端断开通知
//        HttpUtil.syncNetworkStatus(clientIp, 0);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
//        cause.printStackTrace();
        Channel ch = ctx.channel();

        ChannelManager.logout(ch);
        ChannelManager.removeChannelFromGroup(ch);
        log.error("{} -> [异常]原因：{}", ch.remoteAddress().toString(),
                cause.getMessage());
    }
}
