package com.fengxu.http.proxy;


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

        public Builder baseUrl(String url){
            this.baseUrl = url;
            return this;
        }

        public Builder startLog(boolean isStart){
            if(isStart){
                fxHttpProxy.startLog();
            }
            return this;
        }

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
