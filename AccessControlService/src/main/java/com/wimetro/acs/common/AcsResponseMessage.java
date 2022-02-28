package com.wimetro.acs.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @title: AcsResponseMessage
 * @author: Ellie
 * @date: 2022/02/10 14:17
 * @description:
 **/
@Data
@NoArgsConstructor
public class AcsResponseMessage extends Message<OperationResult>{
    @Override
    public Class getMessageBodyDecodeClass(int opcode) {
        return OperationType.fromOpCode(opcode).getOperationResultClazz();
    }

    public AcsResponseMessage(int opCode, OperationResult result){
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMsgType(opCode);
        this.setMessageHeader(messageHeader);
        this.setMessageBody(result);
    }

    @Override
    public String toString() {
        MessageHeader header = this.getMessageHeader();
        MessageBody body = this.getMessageBody();

        return header.toString() + " " + body.toString();
    }
}
