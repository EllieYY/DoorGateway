package com.wimetro.acs.server.runner;

import com.wimetro.acs.common.Constants;
import com.wimetro.acs.server.codec.*;
import com.wimetro.acs.server.handler.AcceptorIdleStateTrigger;
import com.wimetro.acs.server.handler.ServerProcessHandler;
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
    private String webKey;
    private int devicePort;
    private int webPort;

    public NettyServerInitializer(UnorderedThreadPoolEventExecutor businessGroup, String webKey,
                                  int devicePort, int webPort) {
        this.businessGroup = businessGroup;
        this.webKey = webKey;
        this.devicePort = devicePort;
        this.webPort = webPort;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        int localPort = socketChannel.localAddress().getPort();
        if (localPort == webPort) {
            pipeline.addLast("webFrameDecoder", new WebFrameDecoder());
            pipeline.addLast("webProtocolDecoder", new WebProtocolDecoder(devicePort));
            pipeline.addLast("webProtocolEncoder", new WebProtocolEncoder());
        } else if (localPort == devicePort) {
            pipeline.addLast(new IdleStateHandler(
                    Constants.SERVER_READ_IDEL_TIME_OUT,
                    Constants.SERVER_WRITE_IDEL_TIME_OUT,
                    Constants.SERVER_ALL_IDEL_TIME_OUT,
                    TimeUnit.SECONDS));
            pipeline.addLast(new AcceptorIdleStateTrigger());
            pipeline.addLast("deviceFrameDecoder", new DeviceFrameDecoder());
            pipeline.addLast("deviceProtocolDecoder", new DeviceProtocolDecoder(webKey));
            pipeline.addLast("deviceProtocolEncoder", new DeviceProtocolEncoder());

        } else {
            log.error("请检查服务监听端口配置，不支持端口号{}的数据处理。", localPort);
        }

        pipeline.addLast(businessGroup,"processHandler", new ServerProcessHandler());
        //根据端口动态的选择解码器
//        pipeline.addLast(new NettyServerDecoder(localPort));
//        pipeline.addLast(new NettyServerHandler());

    }
}
