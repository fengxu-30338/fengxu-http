package com.fengxu.http.proxy;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * http 拦截器，匹配特定规则请求，添加请求头，参数，配置规则依据@Fxhttp注解
 * 中的value属性，以正则表达式的方式进行匹配
 *
 * @Author 风珝
 * @Date 2021/4/11 15:12
 * @Version 1.0.0
 */
public class FxHttpInterceptor {

    // 请求头
    protected Map<String, String> headers = new HashMap<>();

    // 请求表单
    protected Map<String, Object> forms = new HashMap<>();

    protected FxHttpInterceptor() {
    }

    protected FxHttpInterceptor(Map<String, String> headers, Map<String, Object> forms) {
        this.headers = headers;
        this.forms = forms;
    }

    /**
     * 添加请求头
     *
     * @param name  请求头的键
     * @param value 请求头的值
     * @Author 风珝
     * @Date 2021/4/11 15:23
     * @Version 1.0.0
     */
    public FxHttpInterceptor addHeader(@NotNull String name, @NotNull String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param header 请求头键值对
     * @Author 风珝
     * @Date 2021/4/11 15:23
     * @Version 1.0.0
     */
    public FxHttpInterceptor addHeader(@NotNull Map<String, String> header) {
        for (Map.Entry<String, String> entry : header.entrySet()) {
            headers.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 添加表单
     *
     * @param name  参数名
     * @param value 参数值
     * @Author 风珝
     * @Date 2021/4/11 15:23
     * @Version 1.0.0
     */
    public FxHttpInterceptor addForm(@NotNull String name, @NotNull Object value) {
        forms.put(name, value);
        return this;
    }

    /**
     * 添加表单
     *
     * @param form 请求表单
     * @Author 风珝
     * @Date 2021/4/11 15:23
     * @Version 1.0.0
     */
    public FxHttpInterceptor addForm(@NotNull Map<String, Object> form) {
        for (Map.Entry<String, Object> entry : form.entrySet()) {
            forms.put(entry.getKey(), entry.getValue());
        }
        return this;
    }


    /**
     * 获取拦截到的请求表单
     *
     * @return 请求表单
     * @Author 风珝
     * @Date 2021/4/11 22:47
     * @Version 1.0.0
     */
    public Map<String, Object> getForm() {
        return forms;
    }

    /**
     * 获取拦截到的请求的请求头信息
     *
     * @return 请求表单
     * @Author 风珝
     * @Date 2021/4/11 22:47
     * @Version 1.0.0
     */
    public Map<String, String> getHeader() {
        return headers;
    }
}
