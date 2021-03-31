package com.fengxu.http.exception;

/**
 * 数据访问异常
 * @Author 风珝
 * @Date 2021/3/26 12:35
 * @Version 1.0.0
 */
public class DataAccessException extends RuntimeException{

    public DataAccessException(String message) {
        super(message);
    }
}
