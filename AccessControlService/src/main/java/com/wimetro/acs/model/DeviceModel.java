/*
 * 版本:v1.0.1
 *   修改时间:
 *   作者:段焱明
 *   内容:
 *
 *                   CopyRight  Fiberhome  All Rights Reservee
 *                      武汉烽火信息集成技术有限公司版权所有
 */

package com.wimetro.acs.model;

/**
 * 控制器设备信息描述
 * User: 焱明
 * Date: 13-8-9
 * Time: 上午10:09
 */
public class DeviceModel {

    private String devIp;   // 设备Ip
    private String devMac;  //设备mac地址
    private String devName; // 设备名称

    public String getDevIp() {
        return devIp;
    }

    public void setDevIp(String devIp) {
        this.devIp = devIp;
    }

    public String getDevMac() {
        return devMac;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

}
