package com.wimetro.acs.server.runner;

import com.wimetro.acs.server.codec.DeviceFrameDecoder;
import com.wimetro.acs.server.codec.DeviceProtocolDecoder;
import com.wimetro.acs.server.codec.DeviceProtocolEncoder;
import com.wimetro.acs.server.handler.ServerProcessHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @title: Server
 * @author: Ellie
 * @date: 2022/02/09 09:29
 * @description:
 **/
@Slf4j
//@Component
public class NettyServerRunner implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    @Value("${netty.tcp.port}")
    private int port;
    @Value("${netty.tcp.connections}")
    private int connections;

    private ApplicationContext applicationContext;
    private Channel serverChannel;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.channel(NioServerSocketChannel.class);

        bootstrap.option(ChannelOption.SO_BACKLOG, connections)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000);

        //thread
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("boss"));
        NioEventLoopGroup workGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        UnorderedThreadPoolEventExecutor businessGroup = new UnorderedThreadPoolEventExecutor(
                10, new DefaultThreadFactory("business"));

        try {
            bootstrap.group(bossGroup, workGroup);
            //log
            LoggingHandler debugLogHandler = new LoggingHandler(LogLevel.DEBUG);
            LoggingHandler infoLogHandler = new LoggingHandler(LogLevel.INFO);

            bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

//                    pipeline.addLast("debegLog", debugLogHandler);
                    pipeline.addLast("idleHandler",
                            new IdleStateHandler(20, 0, 0));

                    pipeline.addLast("frameDecoder", new DeviceFrameDecoder());
                    pipeline.addLast("protocolDecoder", new DeviceProtocolDecoder(""));
                    pipeline.addLast("protocolEncoder", new DeviceProtocolEncoder());

                    pipeline.addLast("infolog", infoLogHandler);

                    pipeline.addLast(businessGroup, new ServerProcessHandler());

//                    pipeline.addLast("keepalive", new KeepaliveHandler());
                }
            });

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            this.serverChannel = channelFuture.channel();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.warn("服务链路中断", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            businessGroup.shutdownGracefully();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (this.serverChannel != null) {
            this.serverChannel.close();
        }
        log.info("服务停止");
    }
}
