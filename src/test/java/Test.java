import com.alibaba.fastjson.JSONObject;
import com.fengxu.http.proxy.FxHttpMain;

import java.io.File;
import java.security.MessageDigest;


public class Test {

    public static void main(String[] args) throws Exception {
        MusicFunc musicFunc = new FxHttpMain.Builder()
                .baseUrl("http://47.96.229.28")
                .startLog(true)
                .addInterceptor(http->{
                    http.addHeader("uid","AAA");
                    String md5Str = null;
                    try {
                        md5Str = md5("AAA" + "SDSVUHA!%^SA");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    http.addHeader("s", md5Str.substring(2,18).toLowerCase());
                },"/")
                .build(MusicFunc.class);
        String list = musicFunc.getMusicList(0, 0, 10);
        System.out.println(list);
        list = musicFunc.getMusicList(1, 10, 10);
        System.out.println(list);
    }

    public static String md5(String str) throws Exception{
        MessageDigest instance = MessageDigest.getInstance("MD5");
        byte[] digest = instance.digest(str.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            //获取低八位有效值
            int i = (int)b & 0xff;
            //将整数转化为16进制
            String hexString = Integer.toHexString(i);
            if (hexString.length() < 2) {
                //如果是一位的话，补0
                hexString = "0" + hexString;
            }
            sb.append(hexString);
        }
        return sb.toString().toLowerCase();
    }

}
