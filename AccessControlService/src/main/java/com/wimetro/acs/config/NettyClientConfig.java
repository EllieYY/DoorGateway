package com.wimetro.acs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @title: NettyClientConfig
 * @author: Ellie
 * @date: 2022/02/28 09:31
 * @description:
 **/
@ConfigurationProperties("netty")
@Configuration
@Data
public class NettyClientConfig {
    private int deviceClientPort;
    private int webClientPort;
    private String webClientIp;
}
