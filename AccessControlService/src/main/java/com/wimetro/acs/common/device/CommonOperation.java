package com.wimetro.acs.common.device;

import com.wimetro.acs.util.ProtocolFiledUtil.CmdProp;
import com.wimetro.acs.common.Operation;
import com.wimetro.acs.common.OperationResult;
import lombok.Data;

/**
 * @title: CommonOperation
 * @author: Ellie
 * @date: 2022/02/10 16:10
 * @description: 返回报文体为空的operation
 **/
@Data
public class CommonOperation extends Operation {
    @CmdProp(index = 1)
    private String context;

    @Override
    public OperationResult execute() {
        CommonOperationResult response = new CommonOperationResult("");
        return response;
    }
}
