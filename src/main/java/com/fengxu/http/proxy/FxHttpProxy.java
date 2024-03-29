package com.fengxu.http.proxy;

import com.fengxu.http.FxHttp;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * 动态代理生成类
 *
 * @Author 风珝
 * @Date 2021/3/17 12:47
 * @Version 1.0.0
 */
class FxHttpProxy implements InvocationHandler {

    // 带http/https的Http域名或IP地址
    private String baseURl = null;

    // 是否输出日志
    private boolean canOutLog = false;

    // 拦截器行为
    private Map<List<Pattern>, Consumer<FxHttpInterceptor>> interceptorMap = new LinkedHashMap<>();

    // 方法映射存储列表
    private List<HttpProp> httpPropList = new ArrayList<>();

    // Http方法处理实现类
    private IHttpHandler httpHandler;


    /**
     * 生成代理对象
     *
     * @param target 目标对象字节码
     * @return 代理对象
     * @Author 风珝
     * @Date 2021/3/17 17:30
     * @Version 1.0.0
     */
    public <T> T generateProxy(Class<T> target) {
        try {
            Field baseUrl = target.getDeclaredField("baseUrl");
            baseUrl.setAccessible(true);
            Object fieldVal = baseUrl.get(target);
            if (fieldVal instanceof String) {
                this.baseURl = (String) fieldVal;
            } else {
                throw new IllegalArgumentException("The value of baseurl you defined should be of string type!");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get baseurl");
        }
        T t = (T) Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, this);
        this.selectHttpHandler();
        this.handlerMethod(target);
        return t;
    }

    /**
     * 生成代理对象
     *
     * @param target  目标对象字节码
     * @param baseURl 设置基础URL
     * @return 代理对象
     * @Author 风珝
     * @Date 2021/3/17 17:30
     * @Version 1.0.0
     */
    public <T> T generateProxy(Class<T> target, String baseURl) {
        this.baseURl = baseURl;
        T t = (T) Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, this);
        this.selectHttpHandler();
        this.handlerMethod(target);
        return t;
    }


    /**
     * 代理方法处理逻辑
     *
     * @param proxy  代理对象
     * @param method 方法反射对象
     * @param args   方法参数
     * @return
     * @Author 风珝
     * @Date 2021/3/19 14:37
     * @Version 1.0.0
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        HttpProp httpProp = null;
        for (HttpProp hp : httpPropList) {
            if (hp.getMethod().equals(method)) {
                httpProp = hp;
                break;
            }
        }
        if (httpProp == null) {
            throw new IllegalArgumentException("The @FxHttp annotation is not used in this method");
        }
        httpProp.initProp();
        return httpHandler.parseHttpRequest(httpProp, args);
    }


    /**
     * 获取代理接口中的所有被@FxHttp注解标注过的方法信息
     *
     * @param tClass 字节码类型
     * @Author 风珝
     * @Date 2021/3/17 16:04
     * @Version 1.0.0
     */
    private void handlerMethod(Class<?> tClass) {
        for (Method method : tClass.getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(FxHttp.class)) {
                FxHttp fxHttp = method.getAnnotation(FxHttp.class);
                HttpProp httpProp = new HttpProp(method, fxHttp);
                httpProp.setInterceptorMap(interceptorMap);
                httpProp.setCanOutLog(this.canOutLog);
                if (fxHttp.url().isEmpty()) {
                    httpProp.setSourceUrl(this.baseURl + fxHttp.value());
                } else {
                    httpProp.setSourceUrl(fxHttp.url());
                }
                httpProp.copyToSendUrl();
                this.httpPropList.add(httpProp);
            } else {
                System.out.println(String.format("Method:%s will not be proxied", method.getName()));
            }
        }
    }


    /**
     * 选择当前项目的HttpHandler的实现类
     *
     * @Author 风珝
     * @Date 2021/3/19 15:29
     * @Version 1.0.0
     */
    private void selectHttpHandler() {
        try {
            Class.forName("okhttp3.OkHttpClient");
            this.httpHandler = HttpOkHandler.getInstance();
            System.out.println("=============fxhttp V0.2.4----okhttp===============");
            return;
        } catch (ClassNotFoundException e) {
            // not use OkHttp
        }
        try {
            Class.forName("cn.hutool.http.HttpRequest");
            this.httpHandler = HttpHuToolHandler.getInstance();
            System.out.println("=============fxhttp V0.2.4----hutool================");
            return;
        } catch (ClassNotFoundException e) {
            // not use hutool
        }
        if (this.httpHandler == null) {
            throw new RuntimeException("You did not import OkHttp or Hutool-http dependency");
        }
    }

    /**
     * 开启日志打印
     *
     * @Author 风珝
     * @Date 2021/3/31 19:06
     * @Version 1.0.0
     */
    public void startLog() {
        this.canOutLog = true;
    }


    /**
     * 添加拦截器
     *
     * @param action 拦截器回调
     * @param regex  正则表达式
     * @Author 风珝
     * @Date 2021/4/12 12:57
     * @Version 1.0.0
     */
    public void addInterceptor(@NotNull Consumer<FxHttpInterceptor> action, String... regex) {
        List<Pattern> patterns = new ArrayList<>();
        for (String r : regex) {
            patterns.add(Pattern.compile(r));
        }
        interceptorMap.put(patterns, action);
    }

    /**
     * 添加拦截器
     *
     * @param action   拦截器回调
     * @param patterns 正则表达式对象
     * @Author 风珝
     * @Date 2021/4/12 12:57
     * @Version 1.0.0
     */
    public void addInterceptor(@NotNull Consumer<FxHttpInterceptor> action, Pattern... patterns) {
        List<Pattern> patternList = new ArrayList<>();
        for (Pattern pattern : patterns) {
            patternList.add(pattern);
        }
        interceptorMap.put(patternList, action);
    }
}


