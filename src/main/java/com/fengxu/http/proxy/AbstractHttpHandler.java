package com.fengxu.http.proxy;

import com.fengxu.http.*;
import com.fengxu.http.exception.DataAccessException;

import java.io.File;
import java.lang.annotation.Annotation;

/**
 * 默认实现了IHttpHandler的抽象类
 *
 * @Author 风珝
 * @Date 2021/3/31 20:19
 * @Version 1.0.0
 */
abstract class AbstractHttpHandler implements IHttpHandler {


    /**
     * 获取指定参数类型或子类的位置
     *
     * @param args 参数数组
     * @return 参数索引, 找不到则返回-1
     * @Author 风珝
     * @Date 2021/3/19 20:16
     * @Version 1.0.0
     */
    protected int findParameterPos(Object[] args, Class<?> tClass) {
        if (args == null) {
            return -1;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && tClass.isAssignableFrom(args[i].getClass())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 解析参数上的注解信息
     *
     * @param httpProp             方法封装对象
     * @param parameterAnnotations 该参数上的所有注解信息
     * @param arg                  该参数的值
     * @return 该参数是否被注解标注过
     * @Author 风珝
     * @Date 2021/3/25 18:34
     * @Version 1.0.0
     */
    protected boolean parseParameterAnnotation(HttpProp httpProp,
                                               Annotation[] parameterAnnotations,
                                               Object arg) {
        // 解析@FxHttp的headers参数到请求头
        String[] headers = httpProp.getFxHttp().headers();
        if (headers.length > 0) {
            for (String header : headers) {
                String[] splitHeader = header.split(":");
                if (splitHeader.length != 2) {
                    throw new IllegalArgumentException("error headers : " + header);
                }
                httpProp.addHeader(splitHeader[0], splitHeader[1]);
            }
        }

        // 是否被本框架提供的参数注解标注过
        boolean isParse = false;

        // 获取参数上的@FxQuery信息
        for (Annotation annotation : parameterAnnotations) {
            if (FxQuery.class.isAssignableFrom(annotation.getClass())) {
                // 该注解是FxQuery
                httpProp.addForm(((FxQuery) annotation).value(), arg);
                isParse = true;
                continue;
            }
            if (FxHeader.class.isAssignableFrom(annotation.getClass())) {
                // 该注解是FxHeader
                httpProp.addHeader(((FxHeader) annotation).value(), String.valueOf(arg));
                isParse = true;
                continue;
            }
            if (FxPath.class.isAssignableFrom(annotation.getClass())) {
                // 该注解是FxPath
                String sendUrl = httpProp.getSendUrl().replace(String.format("{%s}",
                        ((FxPath) annotation).value()), String.valueOf(arg));
                httpProp.setSendUrl(sendUrl);
                isParse = true;
                continue;
            }
            if (FxFilename.class.isAssignableFrom(annotation.getClass())) {
                // 该注解是FxFilename
                if (!arg.getClass().equals(String.class)) {
                    throw new DataAccessException("filename mast be String");
                }
                httpProp.getFileProp().setFilename(String.valueOf(arg));
                isParse = true;
                continue;
            }
            if (FxFile.class.isAssignableFrom(annotation.getClass())) {
                // 该注解是FxFile
                FxFile fxFile = (FxFile) annotation;
                if (arg.getClass().equals(File.class)) {
                    // 文件为File类型
                    httpProp.getFileProp().setFile(fxFile.value(), (File) arg);
                    if (!fxFile.filename().isEmpty()) {
                        httpProp.getFileProp().setFilename(fxFile.filename());
                    }
                    isParse = true;
                    continue;
                }
                if (arg.getClass().equals(byte[].class)) {
                    // 当文件体为byte[]类型时，必须制定文件名
                    if (httpProp.getFileProp().getFilename() == null && fxFile.filename().isEmpty()) {
                        throw new DataAccessException("filename cannot be empty");
                    }
                    httpProp.getFileProp().setBytes(fxFile.value(), fxFile.filename(), (byte[]) arg);
                    isParse = true;
                    continue;
                }
            }
        }

        return isParse;
    }

}
