package com.fengxu.http.exception;

/**
 * Json格式化异常
 *
 * @Author 风珝
 * @Date 2021/3/26 12:53
 * @Version 1.0.0
 */
public class JsonFormatException extends RuntimeException {
    public JsonFormatException(String message) {
        super(message);
    }
}
