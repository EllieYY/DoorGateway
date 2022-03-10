package com.wimetro.acs.common.device;

import com.wimetro.acs.util.ProtocolFiledUtil.CmdProp;
import com.wimetro.acs.common.Operation;
import com.wimetro.acs.common.OperationResult;
import lombok.Data;

/**
 * @title: CommonOperation
 * @author: Ellie
 * @date: 2022/02/10 16:10
 * @description: 报文体部分透传
 **/
@Data
public class CommonOperationReSameBody extends Operation {
    @CmdProp(index = 1)
    private String context;

    @Override
    public OperationResult execute() {
        CommonOperationResult response = new CommonOperationResult(context);
        return response;
    }
}
