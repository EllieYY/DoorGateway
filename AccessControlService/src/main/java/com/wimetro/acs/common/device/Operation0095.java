package com.wimetro.acs.common.device;

import com.wimetro.acs.common.Operation;
import com.wimetro.acs.common.OperationResult;

/**
 * @title: Operation0095
 * @author: Ellie
 * @date: 2022/02/28 11:12
 * @description:
 **/
public class Operation0095 extends Operation {
    @Override
    public OperationResult execute() {
        // 默认查询控制器接口状态
        CommonOperationResult response = new CommonOperationResult("32;");
        return response;
    }
}
