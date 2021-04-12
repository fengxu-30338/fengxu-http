package com.fengxu.http.proxy;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Http接口代理对象获取类
 * @Author 风珝
 * @Date 2021/3/17 17:37
 * @Version 1.0.0
 */
public class FxHttpMain {

    /**
     * 使用建造者模式构建对象
     * @Author 风珝
     * @Date 2021/3/31 19:20
     * @Version 1.0.0
     */
    public static class Builder{

        private FxHttpProxy fxHttpProxy = new FxHttpProxy();

        private String baseUrl = null;

        /**
         * 设置Http基Url
         * @param url url路径
         * @return
         */
        public Builder baseUrl(@NotNull String url){
            this.baseUrl = url;
            return this;
        }

        /**
         * 是否开启日志打印
         * @param isStart 是否开启
         * @return
         */
        public Builder startLog(boolean isStart){
            if(isStart){
                fxHttpProxy.startLog();
            }
            return this;
        }

        /**
         * 添加拦截器
         *
         * @param  action 拦截器回调
         * @param  regex 正则表达式
         * @Author 风珝
         * @Date 2021/4/12 12:57
         * @Version 1.0.0
         */
        public Builder addInterceptor(@NotNull Consumer<FxHttpInterceptor> action, String... regex) {
            fxHttpProxy.addInterceptor(action,regex);
            return this;
        }

        /**
         * 添加拦截器
         *
         * @param  action 拦截器回调
         * @param  patterns 正则表达式对象
         * @Author 风珝
         * @Date 2021/4/12 12:57
         * @Version 1.0.0
         */
        public Builder addInterceptor(@NotNull Consumer<FxHttpInterceptor> action, Pattern... patterns) {
            fxHttpProxy.addInterceptor(action,patterns);
            return this;
        }

        /**
         * 构建代理
         * @param tClass 目标对象的字节码
         * @return 代理对象
         */
        public <T> T build(Class<T> tClass){
            if(baseUrl != null){
                return fxHttpProxy.generateProxy(tClass,baseUrl);
            }
            return fxHttpProxy.generateProxy(tClass);
        }

    }

    // 获取代理对象
    @Deprecated
    public static <T> T getProxy(Class<T> tClass){
        return new FxHttpProxy().generateProxy(tClass);
    }

    // 获取代理对象
    @Deprecated
    public static <T> T getProxy(Class<T> tClass, String baseUrl){
        return new FxHttpProxy().generateProxy(tClass, baseUrl);
    }
}
