package com.wimetro.acs.common.device;

import com.wimetro.acs.common.Operation;
import com.wimetro.acs.common.OperationResult;
import com.wimetro.acs.util.ProtocolFiledUtil.CmdProp;

/**
 * @title: CommonOperationWithoutBody
 * @author: Ellie
 * @date: 2022/03/01 11:04
 * @description: 响应部分不包含报文体
 **/
public class CommonOperationReNoneBody extends Operation {
    @CmdProp(index = 1)
    private String context;

    @Override
    public OperationResult execute() {
        CommonOperationResult response = new CommonOperationResult("");
        return response;
    }
}
