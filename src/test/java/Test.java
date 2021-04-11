import com.alibaba.fastjson.JSONObject;
import com.fengxu.http.proxy.FxHttpMain;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class Test {

    public static void main(String[] args) throws Exception {
        FxTest fxTest = new FxHttpMain.Builder().startLog(true)
                .setInterceptor(fxHttpInterceptor -> {
                    fxHttpInterceptor.addPattern("/api/*")
                            .addForm("test","测试数据")
                            .addHeader("token","token");
                })
                .build(FxTest.class);
        String s = fxTest.login("风珝", "123321");
        System.out.println(s);
    }

    private static void test4(){
        MusicHttp musicHttp = new FxHttpMain.Builder().startLog(true).build(MusicHttp.class);
        String music = musicHttp.getMusic(1, "name", "netease", "我和你");
        System.out.println(music);
    }

    private static void test3() throws Exception {
        MusicHttp musicHttp = new FxHttpMain.Builder().startLog(true).build(MusicHttp.class);
        String token = musicHttp.getToken("1964075703@qq.com", "123321abc");
        token = JSONObject.parseObject(token).get("data").toString();
        token = JSONObject.parseObject(token).getString("token");

        File file = new File("C:\\Users\\Administrator\\Pictures\\Camera Roll\\4984.jpg");
        System.out.println(file.exists());
        String res = musicHttp.uploadFile(file,token,"1.png");
        System.out.println(res);
    }


    private static void test1(){
        FxTest fxTest = new FxHttpMain.Builder().build(FxTest.class);
//        Map<String,Object> params = new HashMap<>();
//        params.put("username","风珝");
//        params.put("password","123321");
//        String user = fxTest.getUser(params);
//        System.out.println(user);

        String s = fxTest.pubExamInfo(1, "c04619150ee84df7a5d1d8c501025116");
        System.out.println(s);
    }

}
