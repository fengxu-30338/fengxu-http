import com.fengxu.http.FxHttp;
import com.fengxu.http.FxPath;
import com.fengxu.http.HttpMethod;

public interface MusicFunc {

    @FxHttp(value = "/api/list/{type}/{start}/{num}", method = HttpMethod.POST)
    String getMusicList(@FxPath("type") int type,@FxPath("start") int start,@FxPath("num") int num);

}
