package com.wimetro.acs.util.ProtocolFiledUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

/**
 * @title: AcsCmdPropParam
 * @author: Ellie
 * @date: 2022/02/16 15:56
 * @description:
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcsCmdPropParam implements Comparable<AcsCmdPropParam>{
    Integer idx;

    String codec;

    Field field;

    @Override
    public int compareTo(AcsCmdPropParam o) {
        return this.idx.compareTo(o.idx);
    }
}
