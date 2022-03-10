package com.wimetro.acs.netty.runner;

import com.wimetro.acs.common.Constants;
import com.wimetro.acs.config.NettyConfig;
import com.wimetro.acs.netty.codec.*;
import com.wimetro.acs.netty.handler.AcceptorIdleStateTrigger;
import com.wimetro.acs.netty.handler.ServerProcessHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @title: NettyServerInitializer
 * @author: Ellie
 * @date: 2022/02/24 20:56
 * @description:
 **/
@Slf4j
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    UnorderedThreadPoolEventExecutor businessGroup = null;
    private NettyConfig nettyConfig;

    public NettyServerInitializer(UnorderedThreadPoolEventExecutor businessGroup, NettyConfig nettyConfig) {
        this.businessGroup = businessGroup;
        this.nettyConfig = nettyConfig;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        int devicePort = nettyConfig.getDeviceClientPort();
        String webKey = nettyConfig.getWebClientIp() + ":" + nettyConfig.getWebClientPort();

        int localPort = socketChannel.localAddress().getPort();
        if (localPort == nettyConfig.getWebClientPort()) {
            pipeline.addLast("webFrameDecoder", new WebFrameDecoder());
            pipeline.addLast("webProtocolDecoder", new WebProtocolDecoder(devicePort));
            pipeline.addLast("webProtocolEncoder", new WebProtocolEncoder());
        } else if (localPort == devicePort) {
            pipeline.addLast(new IdleStateHandler(
                    Constants.SERVER_READ_IDEL_TIME_OUT,
                    Constants.SERVER_WRITE_IDEL_TIME_OUT,
                    Constants.SERVER_ALL_IDEL_TIME_OUT,
                    TimeUnit.SECONDS));
            pipeline.addLast(new AcceptorIdleStateTrigger(nettyConfig));
            pipeline.addLast("deviceFrameDecoder", new DeviceFrameDecoder());
            pipeline.addLast("deviceProtocolDecoder", new DeviceProtocolDecoder(webKey));
            pipeline.addLast("deviceProtocolEncoder", new DeviceProtocolEncoder());
        } else {
            log.error("请检查服务监听端口配置，不支持端口号{}的数据处理。", localPort);
        }
        pipeline.addLast(businessGroup,"processHandler", new ServerProcessHandler(nettyConfig));
    }
}
