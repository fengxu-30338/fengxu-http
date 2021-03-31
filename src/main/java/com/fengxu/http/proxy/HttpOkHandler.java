package com.fengxu.http.proxy;

import com.alibaba.fastjson.JSONObject;
import com.fengxu.http.FxHttp;
import com.fengxu.http.exception.DataAccessException;
import com.fengxu.http.exception.JsonFormatException;
import com.fengxu.http.okhttpinterface.FxHttpCallback;
import com.fengxu.http.utils.DefaultSSLSocketFactory;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp的方法处理实现类
 *
 * @Author 风珝
 * @Date 2021/3/19 15:27
 * @Version 1.0.0
 */
public class HttpOkHandler extends AbstractHttpHandler {


    private HttpOkHandler(){}

    /**
     * 单例
     */
    private static HttpOkHandler instance;

    /**
     * 获取单例
     *
     * @Author 风珝
     * @Date 2021/3/19 15:09
     * @Version 1.0.0
     */
    public static HttpOkHandler getInstance(){
        if(instance == null){
            synchronized (HttpHuToolHandler.class){
                if(instance == null){
                    instance = new HttpOkHandler();
                }
            }
        }
        return instance;
    }

    /**
     * 获取默认OkHttp客户端,设置默认信任所有证书,且不采用系统代理
     *
     * @Author 风珝
     * @Date 2021/3/19 16:00
     * @Version 1.0.0
     */
    private static OkHttpClient defaultOkHttpClient(FxHttp fxHttp){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .sslSocketFactory(DefaultSSLSocketFactory.getSSLSocketFactory(),
                        DefaultSSLSocketFactory.getX095TrustManager())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                }).callTimeout(fxHttp.timeout(), TimeUnit.MILLISECONDS)
                .proxy(Proxy.NO_PROXY);
        if(fxHttp.connectTimeout() > 0){
            builder.readTimeout(fxHttp.connectTimeout(),TimeUnit.MILLISECONDS);
        }
        if(fxHttp.readTimeout() > 0){
            builder.connectTimeout(fxHttp.connectTimeout(),TimeUnit.MILLISECONDS);
        }
        return builder.build();
    }

    /**
     * 处理http请求
     *
     * @param httpProp 方法信息封装类
     * @param args 方法参数数组
     * @return 经过实体类映射后的结果
     * @Author 风珝
     * @Date 2021/3/19 16:37
     * @Version 1.0.0
     */
    @Override
    public Object parseHttpRequest(HttpProp httpProp, Object[] args) {
        OkHttpClient okHttpClient;
        if(args!= null && args[0].getClass().equals(OkHttpClient.class)){
            okHttpClient = (OkHttpClient) args[0];
        } else {
            okHttpClient = defaultOkHttpClient(httpProp.getFxHttp());
        }

        // 处理方法参数,注解参数，附加值请求中，获取请求体和form表单数据
        parseMethodArgs(httpProp, args);

        // 构建请求对象
        Request request = buildRequest(httpProp);

        // 打印日志
        httpProp.printLogIfCan();

        // 发送请求获取数据
        Response response = sendRequest(okHttpClient, request, args, httpProp.getFxHttp().throwable());

        // 解析结果
        return parseResult(response,httpProp.getMethod().getReturnType(), httpProp.getFxHttp().throwable());
    }


    /**
     * 构造请求
     *
     * @param  httpProp 参数封装对象
     * @return 请求对象
     * @Author 风珝
     * @Date 2021/3/19 19:42
     * @Version 1.0.0
     */
    private static Request buildRequest(HttpProp httpProp){
        Request.Builder req = new Request.Builder();

        // 将请求头信息添加头请求构建器上
        for (Map.Entry<String, String> entry : httpProp.getHeaders().entrySet()) {
            req.addHeader(entry.getKey(), entry.getValue());
        }

        // 从form中构建请求体
        if(httpProp.getParams().size() > 0){
            FormBody.Builder builder = new FormBody.Builder();
            for (Map.Entry<String, Object> entry : httpProp.getParams().entrySet()) {
                builder.add(entry.getKey(),String.valueOf(entry.getValue()));
            }
            httpProp.setBody(builder.build());
        }

        // 解析文件参数
        if(httpProp.getFileProp().containFile()){
            if(httpProp.getFileProp().getFile() != null){
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        // 参数名
                        .addFormDataPart(httpProp.getFileProp().getParamName(),
                                // 文件名
                                httpProp.getFileProp().getFilename(),
                                // 文件的请求体
                                RequestBody.create(httpProp.getFileProp().getFile(),
                                        MediaType.parse("multipart/form-data")))
                        .build();
                httpProp.setBody(requestBody);
            }
            if(httpProp.getFileProp().getBytes() != null){
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart(httpProp.getFileProp().getParamName(),
                                httpProp.getFileProp().getFilename(),
                                RequestBody.create(httpProp.getFileProp().getBytes()
                                        ,MediaType.parse("multipart/form-data")))
                        .build();
                httpProp.setBody(requestBody);
            }
        }

        // 若没有请求体参数则构建默认请求体参数
        if(httpProp.getBody() == null){
            httpProp.setBody(new FormBody.Builder().build());
        }

        // 构建相应的请求
        switch (httpProp.getFxHttp().method()){
            case GET:
                req.url(httpProp.getSendUrl() + httpProp.getParamsString()).get();
                break;
            case POST:
                req.url(httpProp.getSendUrl()).post((RequestBody) httpProp.getBody());
                break;
            case PUT:
                req.url(httpProp.getSendUrl()).put((RequestBody) httpProp.getBody());
                break;
            case PATCH:
                req.url(httpProp.getSendUrl()).patch((RequestBody) httpProp.getBody());
                break;
            case DELETE:
                req.url(httpProp.getSendUrl()).delete((RequestBody) httpProp.getBody());
                break;
            case HEAD:
                req.url(httpProp.getSendUrl()).head();
                break;
            default:
                throw new IllegalArgumentException(httpProp.getFxHttp().method().name() + " type is not supported");
        }
        return req.build();
    }


    /**
     * 发送请求
     * @param okHttpClient OKHttp客户端对象
     * @param request 请求对象
     * @param args 参数信息
     * @param throwable 是否抛出异常
     * @return 异步请求返回null,同步则返回response对象
     */
     private Response sendRequest(OkHttpClient okHttpClient,
                                         Request request,
                                         Object[] args,
                                         boolean throwable){
         // 如果存在callback参数，则使用异步方式请求
         int callBackParameterPos = findParameterPos(args,Callback.class);
         if(callBackParameterPos >= 0){
             okHttpClient.newCall(request).enqueue((Callback)args[callBackParameterPos]);
             return null;
         }

         // 如果存在FxHttpCallback参数，则采用该种方式回调
         int fxHttpCallbackPos = findParameterPos(args,FxHttpCallback.class);
         if(fxHttpCallbackPos >= 0){
             okHttpClient.newCall(request).enqueue(new OkHttpCallback((FxHttpCallback)args[fxHttpCallbackPos]));
             return null;
         }

         // 获取结果，并决定是否抛出异常
         if(throwable){
             try {
                 return okHttpClient.newCall(request).execute();
             } catch (IOException e) {
                 throw new DataAccessException("Failed to get data --> " + e.getMessage());
             }
         }
         // 不抛出异常，直接返回null
         try {
             return okHttpClient.newCall(request).execute();
         } catch (Exception e){
             return null;
         }
     }


    /**
     * 处理方法中传递的参数
     *
     * @param  httpProp 参数包装对象
     * @param args 方法参数数组
     * @Author 风珝
     * @Date 2021/3/19 18:28
     * @Version 1.0.0
     */
    private void parseMethodArgs(HttpProp httpProp, Object[] args){

        // 获取参数信息 安卓中低版本没有getParameters()方法
        Class<?>[] parameterTypes = httpProp.getMethod().getParameterTypes();
        Type[] genericParameterTypes = httpProp.getMethod().getGenericParameterTypes();
        Annotation[][] annotations = httpProp.getMethod().getParameterAnnotations();

        // 解析参数
        for (int i = 0; i < parameterTypes.length; i++) {

            // 参数为空则跳出本次循环
            if(args[i] == null){
                continue;
            }

            // 解析参数上用注解标注过的值,若被注解标注过则不经过下面方法的处理
            if(parseParameterAnnotation(httpProp,annotations[i],args[i])){
                continue;
            }

            if(genericParameterTypes[i] instanceof ParameterizedType){
                /***** 该参数是泛型类型 **********/
                parseGenericParameter(httpProp,parameterTypes[i],genericParameterTypes[i], args[i]);
            } else {
                /***** 该参数是普通类型 **********/
                RequestBody body = parseNormalParameter(args[i]);
                httpProp.setBody(body);
            }
        }
    }


    /**
     * 处理泛型方法中的参数
     *
     * @param  httpProp 参数信息包装对象
     * @param parameterClass 参数类型
     * @param genericParameterType 参数泛型类型
     * @param arg 该参数的值
     * @Author 风珝
     * @Date 2021/3/19 18:39
     * @Version 1.0.0
     */
    private void parseGenericParameter(HttpProp httpProp,
                                              Class<?> parameterClass,
                                              Type genericParameterType,
                                              Object arg){
        if(Map.class.isAssignableFrom(parameterClass) == false){
            return;
        }
        ParameterizedType type = (ParameterizedType) genericParameterType;
        // 获取map中的泛型参数
        Type[] argumentsType = type.getActualTypeArguments();
        if(argumentsType[0].equals(String.class) == false){
            return;
        }
        if(argumentsType[1].equals(String.class)){
            // 该参数是map<String,String>类型为请求头map参数
            httpProp.addHeader((Map<String,String>)arg);
        } else {
            // 该参数是map<String,Object>类型，为表单参数
            httpProp.addForm((Map<String, Object>)arg);
        }
    }

    /**
     * 处理普通方法中的参数
     *
     * @param arg 该参数的值
     * @return 根据该参数获取的请求体
     * @Author 风珝
     * @Date 2021/3/19 18:44
     * @Version 1.0.0
     */
    private static RequestBody parseNormalParameter(Object arg){
        // 该参数是String类型，视其为json请求体
        if(arg instanceof String){
            return RequestBody.create((String) arg,
                    MediaType.parse("application/json; charset=utf-8"));
        }
        // 该参数为byte[]类型，传递字节数组
        if(arg instanceof byte[]){
            return RequestBody.create((byte[]) arg);
        }
        // 该参数为请求体类型，直接获取
        if(RequestBody.class.isAssignableFrom(arg.getClass())){
            return  (RequestBody) arg;
        }
        return null;
    }


    /**
     * 根据放回值类型获取转换后的结果
     *
     * @param  response http响应
     * @param returnType 返回值类型
     * @param throwable 是否抛出异常
     * @return 处理成返回值类型后的数据
     * @Author 风珝
     * @Date 2021/3/19 20:32
     * @Version 1.0.0
     */
    private Object parseResult(Response response,Class<?> returnType,boolean throwable){
        try {
            String res = null;
            if(String.class.equals(returnType)){
                res = response.body().string();
                return res;
            }
            if(InputStream.class.equals(returnType)){
                return response.body().byteStream();
            }
            if(byte[].class.equals(returnType)){
                return response.body().bytes();
            }
            if(Response.class.equals(returnType)){
                return response;
            }
            if(res == null){
                res = response.body().string();
            }
            return JSONObject.parseObject(res,returnType);
        } catch (IOException e) {
            if(throwable){
                throw new DataAccessException("Get data fail -->  " + e.getMessage());
            }else{
                return null;
            }
        }catch (Exception e){
           if(throwable){
               throw new JsonFormatException("Json format error --> " + e.getMessage());
           } else {
               return null;
           }
        }
    }

    /**
     * OkHttp回调Callback实现-封装类
     * @Author 风珝
     * @Date 2021/3/22 12:34
     * @Version 1.0.0
     */
    private static class OkHttpCallback implements Callback{

        private FxHttpCallback fxHttpCallback;

        public OkHttpCallback(FxHttpCallback fxHttpCallback) {
            this.fxHttpCallback = fxHttpCallback;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            fxHttpCallback.apply(false,call,null);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            fxHttpCallback.apply(true,call,response);
        }

    }

}
