package com.wimetro.acs.netty.runner;

import com.wimetro.acs.common.Constants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @title: ChannelGroupsManager
 * @author: Ellie
 * @date: 2022/02/18 14:48
 * @description: 连接管理
 **/
@Slf4j
public class ChannelManager {
    private static final ChannelGroup CHANNEL_GROUPS =
            new DefaultChannelGroup("ChannelGroups", GlobalEventExecutor.INSTANCE);

    private static ConcurrentHashMap<String, Channel> CHANNEL_MAP = new ConcurrentHashMap();
    private static ConcurrentHashMap<ChannelId, Integer> CHANNEL_LOSS_CONNECT_MAP = new ConcurrentHashMap();

    public static String getChannelKey(Channel channel) {
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        String ip = remoteAddress.getAddress().getHostAddress();
        int port = localAddress.getPort();
        String key = ip + Constants.IP_PORT_SPLITTER + port;
        return key;
    }


    // 有效连接：页面连接即有效，设备发送连接报文之后有效
    public static void registry(Channel channel) {
        if (hasRegistered(channel)) {
            return;
        }

        String key = getChannelKey(channel);
        CHANNEL_MAP.put(key, channel);
        log.info("[注册] == {}", key);
    }

    public static boolean hasRegistered(String key) {
        return CHANNEL_MAP.containsKey(key);
    }

    public static boolean hasRegistered(Channel channel) {
        return CHANNEL_MAP.containsValue(channel);
    }

    public static Channel getChannelByIp(String key) {
        return CHANNEL_MAP.get(key);
    }

    public static String getKeyByChannel(Channel channel) {
        String searchResult = CHANNEL_MAP.search(1, (ip, channel1) -> {
            if (channel.id() == channel1.id()) {
                return ip;
            }
            return "";
        });
        return searchResult;
    }


    public static void logout(Channel channel) {
        String key = getChannelKey(channel);
        if (CHANNEL_MAP.containsKey(key)) {
            CHANNEL_MAP.remove(key);
            log.info("[注销] != {}", key);
        }
    }

    public static void logout(String key) {
        if (CHANNEL_MAP.containsKey(key)) {
            Channel channel = CHANNEL_MAP.get(key);
            CHANNEL_MAP.remove(key);
        }
    }

    // channel空闲检测
    public static void updateChannelLossConnectTime(ChannelId channelId, int lossConnectTime) {
        CHANNEL_LOSS_CONNECT_MAP.put(channelId, lossConnectTime);
    }

    public static int getChannelLossConnectTime(ChannelId channelId) {
        Integer value = CHANNEL_LOSS_CONNECT_MAP.get(channelId);
        if (Objects.isNull(value)) {
            return 0;
        } else {
            return value.intValue();
        }
    }


    // group操作管理 ========
    public static void addChannelToGroup(Channel channel) {
        CHANNEL_GROUPS.add(channel);
    }

    public static boolean removeChannelFromGroup(Channel channel) {
        return CHANNEL_GROUPS.remove(channel);
    }

    public static ChannelGroupFuture broadcast(Object msg) {
        return CHANNEL_GROUPS.writeAndFlush(msg);
    }

    public static ChannelGroupFuture broadcast(Object msg, ChannelMatcher matcher) {
        return CHANNEL_GROUPS.writeAndFlush(msg, matcher);
    }

    public static ChannelGroup flush() {
        return CHANNEL_GROUPS.flush();
    }

    public static ChannelGroupFuture disconnect() {
        return CHANNEL_GROUPS.disconnect();
    }

    public static ChannelGroupFuture disconnect(ChannelMatcher matcher) {
        return CHANNEL_GROUPS.disconnect(matcher);
    }

    public static boolean contains(Channel channel) {
        return CHANNEL_GROUPS.contains(channel);
    }



    public static int size() {
        return CHANNEL_GROUPS.size();
    }
}
