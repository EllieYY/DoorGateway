package com.wimetro.acs.netty.runner;

import com.wimetro.acs.common.AcsResponseMessage;
import com.wimetro.acs.netty.codec.DeviceFrameDecoder;
import com.wimetro.acs.netty.codec.DeviceProtocolDecoder;
import com.wimetro.acs.netty.codec.DeviceProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @title: TcpClientPool
 * @author: Ellie
 * @date: 2022/03/01 11:25
 * @description:
 **/
@Slf4j
public class TcpClientPool {
    public static AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;
    private static Bootstrap bootstrap = new Bootstrap();
    private static NioEventLoopGroup group = new NioEventLoopGroup(0, new DefaultThreadFactory("clientPool"));
    private static TcpClientPool tcpClientPool = new TcpClientPool();
    private static final int threadNum = Runtime.getRuntime().availableProcessors();

    static {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY,true);
    }

    public static TcpClientPool getTcpClientPool(){
        return tcpClientPool;
    }

    private TcpClientPool(){
        init();
    }

    public void init() {
        poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress inetSocketAddress) {
            ChannelPoolHandler handler = new ChannelPoolHandler() {
                // 使用完channel需要释放才能放入连接池
                @Override
                public void channelReleased(Channel ch) throws Exception {
                    // TODO Auto-generated method stub
                }

                // 当链接创建的时候添加channelhandler，只有当channel不足时会创建，但不会超过限制的最大channel数
                @Override
                public void channelCreated(Channel ch) throws Exception {
                    log.info("channelCreated. Channel ID: " + ch.id());
                    ch.pipeline().addLast(new DeviceFrameDecoder());
                    ch.pipeline().addLast(new DeviceProtocolDecoder(""));
                    ch.pipeline().addLast(new DeviceProtocolEncoder());
                }

                //获取连接池中的channel
                @Override
                public void channelAcquired(Channel ch) throws Exception {
                    // TODO Auto-generated method stub
                }
            };
            return new FixedChannelPool(bootstrap.remoteAddress(inetSocketAddress), handler, threadNum);
            }
        };
    }

    /**
     * 异步发送消息
     * @param host 目标机器IP
     * @param port 目标机器端口
     * @param message 消息内容
     */
    public void asyncWriteMessage(String host, int port, final AcsResponseMessage message) {
        InetSocketAddress addr = new InetSocketAddress(host, port);
        final SimpleChannelPool pool = TcpClientPool.poolMap.get(addr);
        Future<Channel> future = pool.acquire();
        // 获取到实例后发消息
        future.addListener((FutureListener<Channel>)f -> {
            if (f.isSuccess()) {
                Channel ch = f.getNow();
                if (ch.isWritable()) {
                    ch.writeAndFlush(message);
                }
                // 归还实例
                pool.release(ch);
            }
        });
    }

    /**
     * 同步发送消息
     * @param host 目标机器IP
     * @param port 目标机器端口
     * @param message 消息内容
     * @return 同步获取执行结果
     */
    public boolean syncWriteMessage(String host, int port, final AcsResponseMessage message) {
        InetSocketAddress addr = new InetSocketAddress(host, port);
        final SimpleChannelPool pool = TcpClientPool.poolMap.get(addr);
        Future<Channel> future = pool.acquire();
        try {
            Channel channel = future.get();
            if (channel.isWritable()) {
                channel.writeAndFlush(message);
                pool.release(channel);
                return true;
            }
            pool.release(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        poolMap.close();
        group.shutdownGracefully();
    }
}
