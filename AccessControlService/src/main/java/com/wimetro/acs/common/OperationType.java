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
    DELETE_CARD_RECORD(22, 22, CommonOperation.class, CommonOperationResult.class),
    DELETE_CARD_RECORD_RSP(1022, 1022, CommonOperation.class, CommonOperationResult.class),
    ADD_MOD_CARD_RECORD(24, 24, CommonOperation.class, CommonOperationResult.class),
    ADD_MOD_CARD_RECORD_RSP(1024, 1024, CommonOperation.class, CommonOperationResult.class),
    GET_CARD_RECORD(25, 25, CommonOperation.class, CommonOperationResult.class),
    GET_CARD_RECORD_RSP(1025, 1025, CommonOperation.class, CommonOperationResult.class),

    DISCOVER_INTERFACES(92, 92, CommonOperation.class, CommonOperationResult.class),
    DISCOVER_INTERFACES_RSP(1092, 1092, CommonOperation.class, CommonOperationResult.class),
    CMD_OUTPUT_FUNCTION(94, 94, CommonOperation.class, CommonOperationResult.class),
    CMD_OUTPUT_FUNCTION_RSP(1094, 1094, CommonOperation.class, CommonOperationResult.class),
    KEEPALIVE(95, 95, Operation0095.class, CommonOperationResult.class),

    REBOOT(96, 96, CommonOperation.class, CommonOperationResult.class),
    REBOOT_RSP(1096, 1096, CommonOperation.class, CommonOperationResult.class),
    TASK_RESTART(12, 12, CommonOperation.class, CommonOperationResult.class),
    TASK_RESTART_RSP(1012, 1012, CommonOperation.class, CommonOperationResult.class),

    TIME_SET(18, 18, CommonOperation.class, CommonOperationResult.class),
    TIME_SET_RSP(1018, 1018, CommonOperation.class, CommonOperationResult.class),
    TIME_GET(19, 19, CommonOperation.class, CommonOperationResult.class),
    TIME_GET_RSP(1019, 1019, CommonOperation.class, CommonOperationResult.class),

    WRITE_EEPROM(7, 7, CommonOperation.class, CommonOperationResult.class),
    WRITE_EEPROM_RSP(1007, 1007, CommonOperation.class, CommonOperationResult.class),

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
