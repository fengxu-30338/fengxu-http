package com.fengxu.http.proxy;


/**
 * 处理用户自定义的http映射方法接口
 *
 * @Author 风珝
 * @Date 2021/3/19 14:43
 * @Version 1.0.0
 */
interface IHttpHandler {

    /**
     * 处理http请求
     *
     * @param httpProp http方法信息封装类
     * @param args     方法参数数组
     * @return 经过实体类映射后的结果
     * @Author 风珝
     */
    Object parseHttpRequest(HttpProp httpProp, Object[] args);

}
