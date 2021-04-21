package com.fengxu.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于上传文件的注解
 *
 * @Author 风珝
 * @Date 2021/3/27 21:50
 * @Version 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FxFile {
    String value();

    String filename() default "";
}
