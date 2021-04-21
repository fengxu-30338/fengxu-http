package com.fengxu.http.okhttpinterface;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 基于okhttp-Callback回调函数的封装接口
 *
 * @Author 风珝
 * @Date 2021/3/22 12:17
 * @Version 1.0.0
 */
public interface FxHttpCallback {

    /**
     * 经过封装的Okhttp的回调参数信息，若result为false那么response直接为null
     *
     * @param result   是否成功
     * @param call     okhttp-Call对象
     * @param response Response对象
     * @return Void
     * @Author 风珝
     * @Date 2021/3/22 12:20
     * @Version 1.0.0
     */
    void apply(boolean result, Call call, Response response);
}
