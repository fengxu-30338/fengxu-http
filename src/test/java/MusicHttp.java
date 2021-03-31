import com.fengxu.http.*;

import java.io.File;
import java.util.Map;

public interface MusicHttp {

    String baseUrl = "http://music.234du.com";

    @FxHttp(method = HttpMethod.POST,headers = {"X-Requested-With:XMLHttpRequest"})
    String getMusic(Map<String,Object> params);

    @FxHttp(method = HttpMethod.POST,headers = {"X-Requested-With:XMLHttpRequest"},throwable = false)
    String getMusic(@FxQuery("page") int page, @FxQuery("filter") String filter, @FxQuery("type") String type, @FxQuery("input") String input);


    @FxHttp(url = "https://img.coolcr.cn/api/token", method = HttpMethod.POST)
    String getToken(@FxQuery("email") String email,@FxQuery("password")String pwd);

    @FxHttp(url = "https://img.coolcr.cn/api/upload",method = HttpMethod.POST)
    String uploadFile(@FxFile(value = "image") File file, @FxHeader("token") String token);
}
