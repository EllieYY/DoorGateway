package com.wimetro.acs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @title: StringUtil
 * @author: Ellie
 * @date: 2022/02/14 09:35
 * @description:
 **/
public class StringUtil {
    // 字符串格式化，左补空格
    public static String padLeftSpaces(String str, int n) {
        return String.format("%1$" + n + "s", str);
    }

    public static List<String> getIps(String ipString){
        String regEx="((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
        List<String> ips = new ArrayList<String>();
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(ipString);
        while (m.find()) {
            String result = m.group();
            ips.add(result);
        }
        return ips;
    }
}
