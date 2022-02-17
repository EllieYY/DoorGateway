package com.wimetro.acs.common.device;

import com.wimetro.acs.util.ProtocolFiledUtil.CmdProp;
import com.wimetro.acs.common.OperationResult;
import lombok.Data;

/**
 * @title: CommonOperation
 * @author: Ellie
 * @date: 2022/02/10 15:15
 * @description: body不解析的通用回复
 **/
@Data
public class CommonOperationResult extends OperationResult{

    @CmdProp(index = 1)
    private String context;

    public CommonOperationResult(String context) {
        this.context = context;
    }
}
