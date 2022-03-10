package com.wimetro.acs.netty.handler;

import com.wimetro.acs.netty.runner.TcpClientPool;
import com.wimetro.acs.common.*;
import com.wimetro.acs.common.device.CommonOperationResult;
import com.wimetro.acs.config.NettyConfig;
import com.wimetro.acs.netty.runner.ChannelManager;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @title: AcceptorIdleStateTrigger
 * @author: Ellie
 * @date: 2022/02/24 15:06
 * @description:
 **/
@Slf4j
@ChannelHandler.Sharable
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {
    // 可以把lossConnectTime 放到AttributeMap中
//    private int lossConnectTime = 0;

    private NettyConfig nettyConfig;

    public AcceptorIdleStateTrigger(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress clientSocket = (InetSocketAddress)channel.remoteAddress();
        // 未注册客户端不做空闲检测
        if (!ChannelManager.hasRegistered(channel)) {
            return;
        }
        String clientIp = clientSocket.getAddress().getHostAddress();
        ChannelId channelId = channel.id();

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            IdleState state = event.state();
            if (state == IdleState.READER_IDLE) {
                if (event == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
//                    lossConnectTime = 0;
                    ChannelManager.updateChannelLossConnectTime(channelId, 0);
                    log.info("{} -~ [IdleCheck] first idle happend.", clientIp);
                }
                int lossConnectTime = ChannelManager.getChannelLossConnectTime(channelId);
                lossConnectTime++;
                ChannelManager.updateChannelLossConnectTime(channelId, lossConnectTime);
                log.info("{} ~~ [IdleCheck] {}周期内未检测到通信报文", clientIp, lossConnectTime);

                // 服务端主动维护心跳：获取控制器状态
                OperationResult result = new CommonOperationResult("32");
                AcsResponseMessage responseMessage = new AcsResponseMessage(95, result);
                String channelKey = ChannelManager.getChannelKey(channel);
                responseMessage.getMessageHeader().setTargetIp(channelKey);
                channel.writeAndFlush(responseMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

                // 离线超时判断
                if (lossConnectTime >= Constants.MAX_LOSS_CONNECT_TIME) {
                    log.info("{} !- 离线：server主动断开链路", clientIp);
//                    lossConnectTime = 0;
                    ChannelManager.updateChannelLossConnectTime(channelId, 0);
                    channel.close();

                    // TODO:向4050端口发送重连报文
                    deviceReconnect(clientIp);
                }
            } else {
                log.info("{} -- 恢复连接--", clientIp);
//                lossConnectTime = 0;
                ChannelManager.updateChannelLossConnectTime(channelId, 0);
                super.userEventTriggered(ctx, evt);
            }
        } else {
            log.info("{} -- 恢复连接==", clientIp);
//            lossConnectTime = 0;
            ChannelManager.updateChannelLossConnectTime(channelId, 0);
            super.userEventTriggered(ctx, evt);
        }
    }


    private void deviceReconnect(String deviceIp) {
        // 信息组装
        AcsResponseMessage deviceRequest = new AcsResponseMessage();
        MessageHeader header = new MessageHeader();
        header.setMsgType(45);
        header.setTargetIp(deviceIp + ":" + nettyConfig.getDeviceReconnnectPort());  // 方便显示
        deviceRequest.setMessageHeader(header);
        deviceRequest.setMessageBody(new CommonOperationResult(""));

        // 发送
        log.info("[强制设备重连]{}:{}", deviceIp, nettyConfig.getDeviceReconnnectPort());
        TcpClientPool.getTcpClientPool().asyncWriteMessage(deviceIp, nettyConfig.getDeviceReconnnectPort(), deviceRequest);
    }
}

