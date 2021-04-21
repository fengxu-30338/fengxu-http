package com.fengxu.http.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.fengxu.http.exception.DataAccessException;
import com.fengxu.http.exception.JsonFormatException;
import com.fengxu.http.hutoolnterface.FxHttpConsumer;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * hutool的http方法处理实现类
 *
 * @Author 风珝
 * @Date 2021/3/17 13:10
 * @Version 1.0.0
 */
class HttpHuToolHandler extends AbstractHttpHandler {

    private HttpHuToolHandler() {
    }

    /**
     * 单例
     */
    private static HttpHuToolHandler instance;

    /**
     * 获取单例
     *
     * @Author 风珝
     * @Date 2021/3/19 15:09
     * @Version 1.0.0
     */
    public static IHttpHandler getInstance() {
        if (instance == null) {
            synchronized (HttpHuToolHandler.class) {
                if (instance == null) {
                    instance = new HttpHuToolHandler();
                }
            }
        }
        return instance;
    }


    /**
     * 处理http请求
     *
     * @param httpProp 方法信息封装类
     * @param args     方法参数数组
     * @return 经过实体类映射后的结果
     * @Author 风珝
     * @Date 2021/3/17 17:31
     * @Version 1.0.0
     */
    @Override
    public Object parseHttpRequest(HttpProp httpProp, Object[] args) {

        // 解析参数
        parseMethodParamsToRequest(httpProp, args);

        // 执行拦截器
        httpProp.execInterceptor();

        // 构建请求参数
        HttpRequest httpRequest = buildRequest(httpProp);

        // 打印日志
        httpProp.printLogIfCan();

        // 发起请求
        HttpResponse response = sendRequest(httpRequest, args, httpProp.getFxHttp().throwable());

        // 解析结果并返回
        return parseResult(response, httpProp.getMethod().getReturnType(), httpProp.getFxHttp().throwable());
    }


    /**
     * 逆构建请求
     *
     * @param httpProp Http方法属性封装
     * @return 请求对象
     * @Author 风珝
     * @Date 2021/3/31 20:23
     * @Version 1.0.0
     */
    private HttpRequest buildRequest(HttpProp httpProp) {
        HttpRequest httpRequest = null;
        switch (httpProp.getFxHttp().method()) {
            case GET:
                httpRequest = HttpRequest.get(httpProp.getSendUrl());
                break;
            case POST:
                httpRequest = HttpRequest.post(httpProp.getSendUrl());
                break;
            case DELETE:
                httpRequest = HttpRequest.delete(httpProp.getSendUrl());
                break;
            case HEAD:
                httpRequest = HttpRequest.head(httpProp.getSendUrl());
                break;
            case OPTIONS:
                httpRequest = HttpRequest.options(httpProp.getSendUrl());
                break;
            case PATCH:
                httpRequest = HttpRequest.patch(httpProp.getSendUrl());
                break;
            case PUT:
                httpRequest = HttpRequest.put(httpProp.getSendUrl());
                break;
            case TRACE:
                httpRequest = HttpRequest.trace(httpProp.getSendUrl());
                break;
        }

        // 解析超时时间
        httpRequest.timeout(httpProp.getFxHttp().timeout());
        if (httpProp.getFxHttp().readTimeout() > 0) {
            httpRequest.setReadTimeout(httpProp.getFxHttp().readTimeout());
        }
        if (httpProp.getFxHttp().connectTimeout() > 0) {
            httpRequest.setConnectionTimeout(httpProp.getFxHttp().connectTimeout());
        }

        // 添加请求头
        httpRequest.addHeaders(httpProp.getHeaders());

        // 添加表单参数
        httpRequest.form(httpProp.getParams());

        // 解析文件信息
        if (httpProp.getFileProp().containFile()) {
            if (httpProp.getFileProp().getFile() != null) {
                httpRequest.form(httpProp.getFileProp().getParamName(),
                        httpProp.getFileProp().getFile(),
                        httpProp.getFileProp().getFilename());
            }
            if (httpProp.getFileProp().getBytes() != null) {
                httpRequest.form(httpProp.getFileProp().getParamName(),
                        httpProp.getFileProp().getBytes(),
                        httpProp.getFileProp().getFilename());
            }
        }

        // 解析请求体
        if (httpProp.getBody() != null) {
            if (httpProp.getBody() instanceof String) {
                // 认为是json请求体
                httpRequest.body((String) httpProp.getBody());
            }
            if (httpProp.getBody() instanceof byte[]) {
                httpRequest.body((byte[]) httpProp.getBody());
            }
        }

        return httpRequest;
    }


    /**
     * 发送请求
     *
     * @param request   请求对象
     * @param args      参数信息
     * @param throwable 是否抛出异常
     * @return 返回对象
     * @Author 风珝
     * @Date 2021/3/31 20:47
     * @Version 1.0.0
     */
    private HttpResponse sendRequest(HttpRequest request, Object[] args, boolean throwable) {

        // 判断参数中是否含FxHttpConsumer
        int parameterPos = findParameterPos(args, FxHttpConsumer.class);
        if (parameterPos >= 0) {
            ((FxHttpConsumer) args[parameterPos]).apply(request);
        }

        HttpResponse res = null;
        try {
            res = request.execute();
        } catch (Exception e) {
            if (throwable) {
                throw new DataAccessException("Failed to get data --> " + e.getMessage());
            }
        }
        return res;
    }

    /**
     * 解析参数的类型并将用户自定义参数赋值到请求中
     *
     * @param httpProp 方法参数信息
     * @param args     方法参数数组
     * @Author 风珝
     * @Date 2021/3/18 11:17
     * @Version 1.0.0
     */
    private void parseMethodParamsToRequest(HttpProp httpProp, Object[] args) {
        // 获取参数信息 安卓中低版本没有getParameters()方法
        Class<?>[] parameterTypes = httpProp.getMethod().getParameterTypes();
        Type[] genericParameterTypes = httpProp.getMethod().getGenericParameterTypes();
        Annotation[][] annotations = httpProp.getMethod().getParameterAnnotations();

        // 循环遍历参数
        for (int i = 0; i < parameterTypes.length; i++) {

            // 参数为null跳出本次循环
            if (args[i] == null) {
                continue;
            }

            // 处理注解信息
            if (parseParameterAnnotation(httpProp, annotations[i], args[i])) {
                continue;
            }

            if (genericParameterTypes[i] instanceof ParameterizedType) {
                /***** 该参数是泛型类型 **********/
                if (Map.class.isAssignableFrom(args[i].getClass()) == false) break;
                ParameterizedType type = (ParameterizedType) genericParameterTypes[i];
                // 获取map中的泛型参数
                Type[] argumentsType = type.getActualTypeArguments();
                if (argumentsType[0].equals(String.class) == false) break;
                if (argumentsType[1].equals(String.class)) {
                    // 该参数是map<String,String>类型为请求头map参数
                    httpProp.addHeader((Map<String, String>) args[i]);
                } else {
                    // 该参数是map<String,Object>类型，为表单参数
                    httpProp.addForm((Map<String, Object>) args[i]);
                }
            } else {
                /***** 该参数是普通类型 **********/
                if (args[i] instanceof String) {
                    httpProp.setBody(args[i]);
                    continue;
                }
                if (args[i] instanceof byte[]) {
                    httpProp.setBody(args[i]);
                    continue;
                }
            }
        }
    }


    /**
     * 进行实体类映射
     *
     * @param res       http返回结果
     * @param type      要转换成的实体类类型
     * @param throwable 是否抛出异常
     * @return 实体类对象
     * @Author 风珝
     * @Date 2021/3/17 17:33
     * @Version 1.0.0
     */
    private Object parseResult(HttpResponse res, Class<?> type, boolean throwable) {
        try {
            if (String.class.equals(type)) {
                return res.body();
            }
            if (InputStream.class.equals(type)) {
                return res.bodyStream();
            }
            if (byte[].class.equals(type)) {
                return res.bodyBytes();
            }
            if (HttpResponse.class.equals(type)) {
                return res;
            }
            return JSONObject.parseObject(res.body(), type);
        } catch (Exception e) {
            if (throwable) {
                throw new JsonFormatException("Result conversion failed: " + e.getMessage());
            } else {
                return null;
            }
        }
    }
}
