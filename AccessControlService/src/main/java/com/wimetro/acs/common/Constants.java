package com.wimetro.acs.common;

/**
 * @description 常量定义
 */
public interface Constants {
    String ENCODER_TO_STR = "toStr";

    // 空闲检测参数
    int SERVER_READ_IDEL_TIME_OUT = 300;    // 空闲检测间隔，单位秒
    int SERVER_WRITE_IDEL_TIME_OUT = 0;
    int SERVER_ALL_IDEL_TIME_OUT = 0;
    int MAX_LOSS_CONNECT_TIME = 5;    // 最大连续失联次数

    // 监听端口信息
    int DEVICE_PORT = 4070;
    int WEB_PORT = 4071;

    // 报文结构信息
    int MSG_PREFIX_LENGTH = 16;




}
