package com.wimetro.acs.common;

import com.wimetro.acs.common.device.CommonOperation;
import com.wimetro.acs.common.device.CommonOperationResult;

import java.util.function.Predicate;

/**
 * @title: OperationType
 * @author: Ellie
 * @date: 2022/02/10 14:03
 * @description:
 **/
public enum OperationType {
//    AUTH(1, AuthOperation.class, AuthOperationResult.class),
//    KEEPALIVE(2, KeepaliveOperation.class, KeepaliveOperationResult.class),
    ORDER2(1065, CommonOperation.class, CommonOperationResult.class),
    ORDER1(1042, CommonOperation.class, CommonOperationResult.class),
    ORDER(1080, CommonOperation.class, CommonOperationResult.class);

    private int opCode;
    private Class<? extends Operation> operationClazz;
    private Class<? extends OperationResult> operationResultClazz;

    OperationType(int opCode, Class<? extends Operation> operationClazz, Class<? extends OperationResult> responseClass) {
        this.opCode = opCode;
        this.operationClazz = operationClazz;
        this.operationResultClazz = responseClass;
    }

    public int getOpCode(){
        return opCode;
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
