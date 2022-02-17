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

    public AcsRequestMessage(){}

    public AcsRequestMessage(Long streamId, Operation operation){
//        MessageHeader messageHeader = new MessageHeader();
////        messageHeader.setStreamId(streamId);
//        messageHeader.setMsgType(OperationType.fromOperation(operation).getOpCode());
//        this.setMessageHeader(messageHeader);
//        this.setMessageBody(operation);
    }
}
