package com.fengxu.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于接口方法上的注解
 *
 * @Author 风珝
 * @Date 2021/3/26 12:42
 * @Version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FxHttp {

    // 请求路径
    String value() default "/";

    // 请求路径,若该项部位空则直接使用此url地址
    String url() default "";

    // 请求方法
    HttpMethod method() default HttpMethod.GET;

    // 超时时间(包括连接超时时间和读取超时时间)
    int timeout() default 3000;

    // 连接超时时间(会覆盖timeout设置的时间)大于0生效
    int connectTimeout() default -1;

    // 读取超时时间(会覆盖timeout设置的时间)大于0生效
    int readTimeout() default -1;

    // 请求头参数 "key:value"类型
    String[] headers() default {};

    // 请求超时或者其他状况是否抛出异常，不抛出则返回null
    boolean throwable() default true;

    // 是否匹配多个拦截器
    boolean patterMore() default false;

}
