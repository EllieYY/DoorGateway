package com.wimetro.acs.server.runner;

import com.wimetro.acs.common.Constants;
import com.wimetro.acs.config.NettyClientConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @title: TwoPortNettyServer
 * @author: Ellie
 * @date: 2022/02/24 20:54
 * @description:
 **/
@Slf4j
@Component
public class TwoPortNettyServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;
    UnorderedThreadPoolEventExecutor businessGroup = null;
    ChannelFuture deviceFuture = null;
    ChannelFuture webFuture = null;

    // linux平台下可开启
    boolean epoll = false;

    private ApplicationContext applicationContext;

    private final NettyClientConfig nettyConfig;
    public TwoPortNettyServer(NettyClientConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }


    @PreDestroy
    public void stop() {
        if(deviceFuture !=null) {
            deviceFuture.channel().close().addListener(ChannelFutureListener.CLOSE);
            deviceFuture.awaitUninterruptibly();
            deviceFuture = null;
        }

        if(webFuture !=null) {
            webFuture.channel().close().addListener(ChannelFutureListener.CLOSE);
            webFuture.awaitUninterruptibly();
            webFuture = null;
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        businessGroup.shutdownGracefully();
        log.info(" 服务关闭 ");
    }

    public void start() {
        log.info(" nettyServer 正在启动");

        // thread
        if(epoll) {
            log.info(" nettyServer 使用epoll模式");
            bossGroup = new EpollEventLoopGroup(0, new DefaultThreadFactory("boss"));
            workerGroup = new EpollEventLoopGroup(0, new DefaultThreadFactory("worker"));
        } else {
            log.info(" nettyServer 使用nio模式");
            bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("boss"));
            workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        }
        businessGroup = new UnorderedThreadPoolEventExecutor(
                10, new DefaultThreadFactory("business"));

        // log
        LoggingHandler debugLogHandler = new LoggingHandler(LogLevel.DEBUG);
        LoggingHandler infoLogHandler = new LoggingHandler(LogLevel.INFO);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // option
        if(epoll) {
            serverBootstrap.channel(EpollServerSocketChannel.class);
            serverBootstrap.option(EpollChannelOption.SO_REUSEADDR, true);
        } else {
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(NioChannelOption.SO_REUSEADDR, true);
        }

        String webKey = nettyConfig.getWebClientIp() + ":" + nettyConfig.getWebClientPort();
        serverBootstrap.option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .handler(infoLogHandler)
                .childHandler(new NettyServerInitializer(businessGroup,
                        webKey,
                        nettyConfig.getDeviceClientPort(),
                        nettyConfig.getWebClientPort()));

        // 端口监听
        try {
            serverBootstrap.group(bossGroup, workerGroup);
            deviceFuture = serverBootstrap.bind(nettyConfig.getDeviceClientPort()).sync();
            webFuture = serverBootstrap.bind(nettyConfig.getWebClientPort()).sync();

//            // 同步等待channel关闭
//            deviceFuture.channel().closeFuture().sync();
//            webFuture.channel().closeFuture().sync();

            // 异步等待channel关闭
            deviceFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override public void operationComplete(ChannelFuture future) throws Exception {       //通过回调只关闭自己监听的channel
                    future.channel().close();
                }
            });
            webFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override public void operationComplete(ChannelFuture future) throws Exception {
                    future.channel().close();
                }
            });
        } catch (Exception e){
            log.info("nettyServer 启动时发生异常,{}",e);
            log.info(e.getMessage());
        } finally {
            // 异步等待时注释下面关闭操作
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//            businessGroup.shutdownGracefully();
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.stop();
        log.info("服务停止");
    }
}
