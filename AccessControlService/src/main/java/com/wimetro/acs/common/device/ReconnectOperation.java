package com.wimetro.acs.common.device;

import com.wimetro.acs.common.Operation;
import com.wimetro.acs.common.OperationResult;

/**
 * @title: ReconnectOperation
 * @author: Ellie
 * @date: 2022/03/01 11:11
 * @description: 发送设备重连指令
 **/
public class ReconnectOperation extends Operation {
    @Override
    public OperationResult execute() {
        CommonOperationResult response = new CommonOperationResult("0;");
        return response;
    }
}
