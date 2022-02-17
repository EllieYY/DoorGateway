package com.wimetro.acs.util.ProtocolFiledUtil;

import org.springframework.util.StringUtils;

/**
 * @title: FieldParser
 * @author: Ellie
 * @date: 2022/02/16 15:44
 * @description: 字段解析工具
 **/
public class FieldParser {
    public static String intToStr(Integer val) {
        return val == null ? "" : String.valueOf(val);
    }

    public static Integer strToInt(String val) {
        return StringUtils.hasText(val) ? null : Integer.valueOf(val);
    }

}
