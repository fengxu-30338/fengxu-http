
import com.fengxu.http.*;
import com.fengxu.http.okhttpinterface.FxHttpCallback;

import java.io.OutputStream;
import java.util.Map;

 interface FxTest {

    String baseUrl = "http://8.131.71.174";

    @FxHttp(value = "/user/t")
    String login();

    @FxHttp(value = "/api/pub/notice")
    String getNotice();

    @FxHttp(value = "/api/user/login",method = HttpMethod.POST,timeout = 5000)
    String getUser(Map<String,Object> params);

    @FxHttp(value = "/api/prom/query",method = HttpMethod.POST)
    String getProm(Map<String,Object> params, Map<String,String> headers);

    @FxHttp(value = "/api/pub/pic")
    OutputStream getPic();

    @FxHttp(value = "/api/pub/pic")
    byte[] getPic2();

    @FxHttp(value = "/api/user/regsend", method = HttpMethod.POST)
    void register(Map<String,Object> params, FxHttpCallback callback);

    @FxHttp(value = "/api/exam/pubexam/{page}", method = HttpMethod.POST)
    String pubExamInfo(@FxPath("page") Integer page, @FxHeader("token") String token);


}
