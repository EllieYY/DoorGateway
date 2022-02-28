package com.wimetro.acs.server.handler;

import ch.qos.logback.core.util.ContextUtil;
import com.wimetro.acs.common.*;
import com.wimetro.acs.common.device.CommonOperation;
import com.wimetro.acs.common.device.CommonOperationResult;
import com.wimetro.acs.common.device.Operation0095;
import com.wimetro.acs.server.runner.ChannelManager;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
    //可以把lossConnectTime 放到AttributeMap中
    private int lossConnectTime = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress clientSocket = (InetSocketAddress)channel.remoteAddress();
        // 未注册客户端不做空闲检测
        if (!ChannelManager.hasRegistered(channel)) {
            log.info("has Registered.");
            return;
        }
        String clientIp = clientSocket.getAddress().getHostAddress();

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            IdleState state = event.state();
            if (state == IdleState.READER_IDLE) {
                if (event == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
                    lossConnectTime = 0;
                    log.info("{} - [first idle happen]", clientIp);
                }
                lossConnectTime++;

                // 服务端主动维护心跳：获取控制器状态
                OperationResult result = new CommonOperationResult("32");
                AcsResponseMessage responseMessage = new AcsResponseMessage(95, result);
                channel.writeAndFlush(responseMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

                // 离线超时判断
                log.info("{} ~ [IdleCheck]离线 {} 周期", clientIp, lossConnectTime);
                if (lossConnectTime >= Constants.MAX_LOSS_CONNECT_TIME) {
                    log.info("{} !- 离线：server主动断开链路", clientIp);
                    lossConnectTime = 0;
                    channel.close();

                    // TODO:向4050端口发送重连报文
                }
            } else {
                log.info("{} - 恢复连接--", clientIp);
                lossConnectTime = 0;
                super.userEventTriggered(ctx, evt);
            }
        } else {
            log.info("{} - 恢复连接==", clientIp);
            lossConnectTime = 0;
            super.userEventTriggered(ctx, evt);
        }
    }
}

