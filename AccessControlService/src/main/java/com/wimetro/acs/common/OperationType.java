package com.wimetro.acs.common;

import com.wimetro.acs.common.device.CommonOperation;
import com.wimetro.acs.common.device.CommonOperationResult;
import com.wimetro.acs.common.device.Operation0095;
import com.wimetro.acs.common.keepalive.KeepaliveOperation;
import com.wimetro.acs.common.keepalive.KeepaliveOperationResult;

import java.util.function.Predicate;

/**
 * @title: OperationType
 * @author: Ellie
 * @date: 2022/02/10 14:03
 * @description:
 **/
public enum OperationType {
//    AUTH(1, AuthOperation.class, AuthOperationResult.class),
    READ_CARD(1022, 1022, CommonOperation.class, CommonOperationResult.class),
    READ_CARD_RSP(22, 22, CommonOperation.class, CommonOperationResult.class),
    KEEPALIVE(95, 95, Operation0095.class, CommonOperationResult.class),
    EVENT_PACKAGE(1065, 67, CommonOperation.class, CommonOperationResult.class),
    REGISTRY(1042, 70, CommonOperation.class, CommonOperationResult.class),
    HEART(1080, 80, CommonOperation.class, CommonOperationResult.class);

    private int opCode;
    private int reOpCode;
    private Class<? extends Operation> operationClazz;
    private Class<? extends OperationResult> operationResultClazz;

    OperationType(int opCode, int reOpCode,
                  Class<? extends Operation> operationClazz, Class<? extends OperationResult> responseClass) {
        this.opCode = opCode;
        this.reOpCode = reOpCode;
        this.operationClazz = operationClazz;
        this.operationResultClazz = responseClass;
    }

    public int getOpCode(){
        return opCode;
    }

    public int getReOpCode() {
        return reOpCode;
    }

    public Class<? extends Operation> getOperationClazz() {
        return operationClazz;
    }

    public Class<? extends OperationResult> getOperationResultClazz() {
        return operationResultClazz;
    }

    public static OperationType fromOpCode(int type){
        return getOperationType(requestType -> requestType.opCode == type);
    }

    public static OperationType fromOperation(Operation operation){
        return getOperationType(requestType -> requestType.operationClazz == operation.getClass());
    }

    private static OperationType getOperationType(Predicate<OperationType> predicate){
        OperationType[] values = values();
        for (OperationType operationType : values) {
            if(predicate.test(operationType)){
                return operationType;
            }
        }

        throw new AssertionError("no found type");
    }
}
