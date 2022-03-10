package com.wimetro.acs.netty.handler;

import com.wimetro.acs.netty.runner.TcpClientPool;
import com.wimetro.acs.common.*;
import com.wimetro.acs.common.device.CommonOperationResult;
import com.wimetro.acs.config.NettyConfig;
import com.wimetro.acs.netty.runner.ChannelManager;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @title: ServerProcessHandler
 * @author: Ellie
 * @date: 2022/02/10 15:54
 * @description:
 **/
@Slf4j
public class ServerProcessHandler extends SimpleChannelInboundHandler<AcsRequestMessage> {

    private final NettyConfig nettyConfig;
    @Autowired
    public ServerProcessHandler(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AcsRequestMessage requestMessage) throws Exception {
        Operation operation = requestMessage.getMessageBody();
        OperationResult operationResult = operation.execute();

        // 消息头组装
        int opCode = requestMessage.getMessageHeader().getMsgType();
        int reCode = requestMessage.getResponseOperationCode(opCode);
        if (Objects.isNull(operationResult) || reCode == Constants.NO_RESPONSE_CODE) {
            return;
        }

        // 设备注册
        if (opCode == OperationType.REGISTRY.getOpCode()) {
            ChannelManager.registry(ctx.channel());
        }

        String targetIp = requestMessage.getMessageHeader().getTargetIp();
        String sourceIp = requestMessage.getMessageHeader().getSourceIp();

        AcsResponseMessage responseMessage = new AcsResponseMessage();
        MessageHeader header = new MessageHeader();
        header.setMsgType(reCode);
        header.setSourceIp(sourceIp);
        header.setTargetIp(targetIp);
        responseMessage.setMessageHeader(header);
        responseMessage.setMessageBody(operationResult);

        // 获取发送channel
        Channel targetChannel = ChannelManager.getChannelByIp(targetIp);
        if (Objects.isNull(targetChannel)) {
            log.error("[发送目标未注册] - {}", targetIp);
            return;
        }

        writeToClient(responseMessage, targetChannel);
    }

    private void deviceReconnect(AcsRequestMessage requestMessage) {
        // 报文信息拆解
        String targetIp = requestMessage.getMessageHeader().getTargetIp();
        int splitterIndex = targetIp.indexOf(Constants.IP_PORT_SPLITTER);
        String deviceIp = targetIp.substring(0, splitterIndex);

        // 信息组装
        AcsResponseMessage deviceRequest = new AcsResponseMessage();
        MessageHeader header = new MessageHeader();
        header.setMsgType(requestMessage.getMessageHeader().getMsgType());
        header.setSourceIp(requestMessage.getMessageHeader().getSourceIp());
        header.setTargetIp(deviceIp + ":" + nettyConfig.getDeviceReconnnectPort());  // 方便显示
        deviceRequest.setMessageHeader(header);
        deviceRequest.setMessageBody(new CommonOperationResult(""));

        // 发送
        log.info("[强制设备重连]{}:{}", deviceIp, nettyConfig.getDeviceReconnnectPort());
        TcpClientPool.getTcpClientPool().asyncWriteMessage(deviceIp, nettyConfig.getDeviceReconnnectPort(), deviceRequest);
    }


    private void writeToClient(final AcsResponseMessage responseMessage, Channel channel) {
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
//        log.info("[连接成功] <- {}", ch.remoteAddress().toString().substring(1));
        ChannelManager.addChannelToGroup(ch);

        // 页面连接即有效
        InetSocketAddress serverSocket = (InetSocketAddress)ch.localAddress();
        int localPort = serverSocket.getPort();
        if (localPort == nettyConfig.getWebClientPort()) {
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
//        log.info("[断开连接] !- {}", ch.remoteAddress());

        // 连接状态通知
        InetSocketAddress serverSocket = (InetSocketAddress)ch.localAddress();
        int localPort = serverSocket.getPort();
        // 设备断开需要通知
        if (localPort == nettyConfig.getDeviceClientPort()) {
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

        if (ch.isActive()) {
            ch.close();
        }
        log.error("{} -> [异常]原因：{}", ch.remoteAddress().toString(),
                cause.getMessage());
    }
}
