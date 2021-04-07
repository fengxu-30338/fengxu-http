package com.fengxu.http.proxy;

import com.fengxu.http.FxHttp;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 保存Http接口方法中的属性
 * @Author 风珝
 * @Date 2021/3/31 18:56
 * @Version 1.0.0
 */
class HttpProp {

    // 为解析动态路由前的Url
    private String sourceUrl;

    // 解析后的目的Url
    private String sendUrl;

    // 用户自定义的方法信息
    private Method method;

    // 方法上的fxHttp注解
    private FxHttp fxHttp;

    // 请求头信息
    private Map<String,String> headers = new HashMap<>();

    // 参数信息
    private Map<String,Object> params = new HashMap<>();

    // 文件参数信息
    private FileProp fileProp = new FileProp();

    // 请求体
    private Object body;

    // 是否开启日志打印
    private boolean canOutLog = false;

    public HttpProp(Method method, FxHttp fxHttp) {
        this.method = method;
        this.fxHttp = fxHttp;
    }

    /**
     * 获取表单组成的字符串
     *
     * @return 表单组成的字符串
     * @Author 风珝
     * @Date 2021/3/31 19:02
     * @Version 1.0.0
     */
    public String getParamsString(){
        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue() + "&");
        }
        if(sb.toString().equals("?")){
            return "";
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 如果设置了打印日志则打印
     *
     * @Author 风珝
     * @Date 2021/3/31 22:10
     * @Version 1.0.0
     */
    public void printLogIfCan(){
        if(canOutLog){
            String log = String.format(" ===========>> \n Http(%s): %s \n header -> %s \n form -> %s \n body -> %s \n file -> %s \n ===========>>",
                    fxHttp.method().name(), sendUrl, headers, params, body, fileProp);
            System.out.println(log);
        }
    }

    /**
     * 将源url拷贝至sendUrl
     *
     * @Author 风珝
     * @Date 2021/3/31 19:31
     * @Version 1.0.0
     */
    public void copyToSendUrl(){
        this.sendUrl = this.sourceUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSendUrl() {
        return sendUrl;
    }

    public void setSendUrl(String sendUrl) {
        this.sendUrl = sendUrl;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public FxHttp getFxHttp() {
        return fxHttp;
    }

    public void setFxHttp(FxHttp fxHttp) {
        this.fxHttp = fxHttp;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String name,String value) {
        this.headers.put(name,value);
    }

    public void addHeader(Map<String,String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.put(entry.getKey(),entry.getValue());
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void addForm(String name,Object value) {
        this.params.put(name,value);
    }

    public void addForm(Map<String,Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            this.params.put(entry.getKey(),entry.getValue());
        }
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public boolean isCanOutLog() {
        return canOutLog;
    }

    public void setCanOutLog(boolean canOutLog) {
        this.canOutLog = canOutLog;
    }

    public FileProp getFileProp() {
        return fileProp;
    }

    /**
     * 文件属性类
     * @Author 风珝
     * @Date 2021/3/31 19:59
     * @Version 1.0.0
     */
    public class FileProp{
        // 表单参数名
        private String paramName;
        // 文件名
        private String filename;
        // 文件
        private File file;
        // 字节数组
        private byte[] bytes;

        // 是否包含文件
        public boolean containFile(){
            return file != null || bytes != null;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public File getFile() {
            return file;
        }

        public void setFile(String paramName, File file) {
            this.paramName = paramName;
            setFilename(file.getName());
            this.file = file;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(String paramName,String filename,byte[] bytes) {
            this.paramName = paramName;
            setFilename(filename);
            this.bytes = bytes;
        }

        public String getParamName() {
            return paramName;
        }

        @Override
        public String toString() {
            return "FileProp{" +
                    "paramName='" + paramName + '\'' +
                    ", filename='" + filename + '\'' +
                    ", file=" + file +
                    ", bytes=" + Arrays.toString(bytes) +
                    '}';
        }
    }
}
