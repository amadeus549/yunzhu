package com.hestia.exception;

/**
 * 套餐启用失败异常
 */
public class ServicePackageEnableFailedException extends BaseException {

    public ServicePackageEnableFailedException(){}

    public ServicePackageEnableFailedException(String msg){
        super(msg);
    }
}
