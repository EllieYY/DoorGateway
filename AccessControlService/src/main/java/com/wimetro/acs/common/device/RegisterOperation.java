package com.wimetro.acs.common.device;

import com.wimetro.acs.common.Operation;
import com.wimetro.acs.common.OperationResult;
import com.wimetro.acs.server.runner.ChannelManager;
import com.wimetro.acs.util.ProtocolFiledUtil.CmdProp;
import lombok.Data;

/**
 * @title: RegisterOperation
 * @author: Ellie
 * @date: 2022/02/28 11:25
 * @description:
 **/
@Data
public class RegisterOperation extends Operation {
    @CmdProp(index = 1)
    private String context;

    @Override
    public OperationResult execute() {

        CommonOperationResult response = new CommonOperationResult("");
        return response;
    }
}
