package com.fengxu.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 文件名注解
 *
 * @Author 风珝
 * @Date 2021/4/7 20:10
 * @Version 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FxFilename {
    String value() default "";
}
