package com.wimetro.acs.common;

/**
 * @title: Operation
 * @author: Ellie
 * @date: 2022/02/10 14:02
 * @description:
 **/
public abstract class Operation extends MessageBody{
    public abstract OperationResult execute();
}
