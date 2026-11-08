package com.hestia.handler;

import com.hestia.exception.BaseException;
import com.hestia.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import com.hestia.constant.MessageConstant;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


        /**
     * 捕获数据库异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        // 获取原始报错信息，例如：Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        // 判断是否是重复键错误
        if(message.contains("Duplicate entry")){
            // 按空格切割字符串
            String[] split = message.split(" ");
            // 提取导致冲突的账号名（带单引号，例如 'zhangsan'）
            String username = split[2];
            // 拼接提示信息：'zhangsan' 已存在
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{
            // 如果是其他数据库异常，返回未知错误
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
}
