package com.wimetro.acs.common;

/**
 * @title: AcsResponseMessage
 * @author: Ellie
 * @date: 2022/02/10 14:17
 * @description:
 **/
public class AcsResponseMessage extends Message<OperationResult>{
    @Override
    public Class getMessageBodyDecodeClass(int opcode) {
        return OperationType.fromOpCode(opcode).getOperationResultClazz();
    }
}
