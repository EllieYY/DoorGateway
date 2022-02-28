package com.wimetro.acs.common;

/**
 * @title: AcsRequestMessage
 * @author: Ellie
 * @date: 2022/02/10 14:15
 * @description:
 **/
public class AcsRequestMessage extends Message<Operation>{

    @Override
    public Class getMessageBodyDecodeClass(int opcode) {
        return OperationType.fromOpCode(opcode).getOperationClazz();
    }

    public int getResponseOperationCode(int opcode) {
        return OperationType.fromOpCode(opcode).getReOpCode();
    }

    public AcsRequestMessage(){}

    public AcsRequestMessage(int opCode, Operation operation){
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMsgType(opCode);
        this.setMessageHeader(messageHeader);
        this.setMessageBody(operation);
    }
}
