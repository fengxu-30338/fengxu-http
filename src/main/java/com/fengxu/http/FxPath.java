package com.fengxu.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于替换Url上的{*}
 * @Author 风珝
 * @Date 2021/3/25 17:13
 * @Version 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FxPath {
    String value();
}
