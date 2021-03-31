package com.fengxu.http.hutoolnterface;

import cn.hutool.http.HttpRequest;

public interface FxHttpConsumer {
    void apply(HttpRequest request);
}
