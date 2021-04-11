package com.fengxu.http.proxy;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * http 拦截器，匹配特定规则请求，添加请求头，参数，配置规则依据@Fxhttp注解
 * 中的value属性，以正则表达式的方式进行匹配
 *
 * @Author 风珝
 * @Date 2021/4/11 15:12
 * @Version 1.0.0
 */
public class FxHttpInterceptor {

    // 正则规则列表
    private List<Pattern> patternList = new ArrayList<>();

    // 请求头
    private Map<String,String> headers = new HashMap<>();

    // 请求表单
    private Map<String,Object> forms = new HashMap<>();


    /**
     * 添加正则匹配参数
     *
     * @param  regex 正则表达式
     * @Author 风珝
     * @Date 2021/4/11 15:18
     * @Version 1.0.0
     */
    public FxHttpInterceptor addPattern(@NotNull String... regex){
        for (String r : regex) {
            patternList.add(Pattern.compile(r));
        }
        return this;
    }

    /**
     * 添加正则匹配参数
     *
     * @param  pattern 正则表达式对象
     * @Author 风珝
     * @Date 2021/4/11 15:18
     * @Version 1.0.0
     */
    public FxHttpInterceptor addPattern(@NotNull Pattern pattern){
        patternList.add(pattern);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param name 请求头的键
     * @param value 请求头的值
     * @Author 风珝
     * @Date 2021/4/11 15:23
     * @Version 1.0.0
     */
    public FxHttpInterceptor addHeader(@NotNull String name, @NotNull String value){
        headers.put(name,value);
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
    public FxHttpInterceptor addHeader(@NotNull Map<String,String> header){
        for (Map.Entry<String, String> entry : header.entrySet()) {
            headers.put(entry.getKey(),entry.getValue());
        }
        return this;
    }

    /**
     * 添加表单
     *
     * @param name 参数名
     * @param value 参数值
     * @Author 风珝
     * @Date 2021/4/11 15:23
     * @Version 1.0.0
     */
    public FxHttpInterceptor addForm(@NotNull String name, @NotNull Object value){
        forms.put(name,value);
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
    public FxHttpInterceptor addForm(@NotNull Map<String,Object> form){
        for (Map.Entry<String, Object> entry : form.entrySet()) {
            forms.put(entry.getKey(),entry.getValue());
        }
        return this;
    }

    protected List<Pattern> getPatternList() {
        return patternList;
    }

    protected Map<String, String> getHeaders() {
        return headers;
    }

    protected Map<String, Object> getForms() {
        return forms;
    }
}