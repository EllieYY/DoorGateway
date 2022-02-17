package com.wimetro.acs.util;

/**
 * @title: StringUtil
 * @author: Ellie
 * @date: 2022/02/14 09:35
 * @description:
 **/
public class StringUtil {
    //字符串格式化长度不足补0
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }
}
