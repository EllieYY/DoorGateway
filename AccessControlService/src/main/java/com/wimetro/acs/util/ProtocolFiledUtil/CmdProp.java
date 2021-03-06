package com.wimetro.acs.util.ProtocolFiledUtil;

import com.wimetro.acs.common.Constants;

import java.lang.annotation.*;

/**
 * @title: CmdProp
 * @author: Ellie
 * @date: 2022/02/16 15:10
 * @description:
 **/
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CmdProp {
    int index() default 0;

    String deCodec() default Constants.ENCODER_TO_STR;
    String enCodec() default Constants.ENCODER_TO_STR;
}
