风珝Http映射工具



## 1.添加依赖

```kotlin
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    implementation 'com.github.fengxu-30338:fengxu-http:0.1.1'
    
    // 如果您的安卓项目使用的版本小于8.0建议您添加如下依赖
   	implementation("com.squareup.okhttp3:okhttp:4.9.0")
    
    // 如果您的安卓项目大于等于8.0建议您添加hutool依赖
    implementation("cn.hutool:hutool-http:5.6.0")
    
    /****hutool或okhttp必须选一个，如果都未选择则报错,多个则默认使用okhttp***/
}
```





## 2.开始使用



在安卓开发时，我们往往将http调用写在不同的页面中，这样代码耦合度过高且不易维护，fx-http就是为了解决这一问题而出现的！



### 2.1 快速开始

您只需要将您的http接口方法放如一个单独的java接口文件中，如下:

```java
import com.fengxu.http.*;
import com.fengxu.http.okhttpinterface.FxHttpCallback;
import java.io.OutputStream;
import java.util.Map;

 interface FxTest {

     // 定义该接口下所有方法的统一url前缀
    String baseUrl = "http://192.168.0.102:8000";

    @FxHttp(value = "/api/prom/query",method = HttpMethod.POST)
    String getProm(@FxQuery("page") int page);


    @FxHttp(value = "/api/exam/pubexam/{page}", method = HttpMethod.POST)
    String pubExamInfo(@FxPath("page") Integer page, @FxHeader("token") String token);

}
```



生成该类的代理

```java
// 普通方式生成代理，需要在接口中配置baseUrl
FxTest fxTest = new FxHttpMain.Builder().build(FxTest.class);

// 或者您以可以通过该方法的重载定义该接口中所有方法的统一url前缀
FxTest fxTest = new FxHttpMain.Builder().baseUrl("http: xxxx").build(FxTest.class);

// 您也可以开启日志打印功能
MusicHttp musicHttp = new FxHttpMain.Builder().startLog(true).build(MusicHttp.class);

// 发送请求得到结果
String res = fxTest.getProm(1)
System.out.println(res)
```



### 2.2 @FxHttp注解的使用

该注解标注在http接口方法上，设置该方法的基本属性

| @FxHttp中的属性 | 含义                                                         |
| --------------- | ------------------------------------------------------------ |
| value           | 配合baseUrl真正的url为 baseUrl + FxHttp.value()              |
| url             | 或您想单独为该方法设置url只需要设置其url属性即可如@Fxhttp(url="http://xx.xx") |
| timeout         | 请求的超时时间，默认3000包括(连接超时和读取超时)             |
| connectTimeout  | 连接超时时间，设置了该项会覆盖timeout                        |
| readTimeout     | 读取超时时间，设置了该项会覆盖timeout                        |
| method          | http方法类型(枚举值)如 GET,POST,PUT ...                      |
| headers         | 固定请求头用" : "隔开如 {"token:asdffdg",""X-Requested-With:XMLHttpRequest""} |
| throwable       | 发送请求或解析结果出错时，是否抛出异常,默认true,设置false则错误时，返回值为空 |





### 2.3 @FxQuery注解的使用

该注解用在参数上，表示该参数是一个表单参数，如下:

```java
@FxHttp(url = "https://img.coolcr.cn/api/token", method = HttpMethod.POST)
String getToken(@FxQuery("email") String email,@FxQuery("password")String pwd);
```

其中@FxQuery注解括号中指定的值为表单名称，值为动态传入的参数值！





### 2.4 @FxHeader注解的使用

该注解用在参数上，表示动态请求头，如下:

```java
@FxHttp(value = "/api/find",method = HttpMethod.POST)
String findByEmail(@FxHeader("token") String token, @FxQuery("email") String email);
```

其中@FxHeader注解括号中指定的值为请求头名称，值为动态传入的参数值！



### 2.5 @FxFile注解的使用

该注解用在参数上，表示传输文件，如下:

```java
@FxHttp(url = "https://img.coolcr.cn/api/upload",method = HttpMethod.POST)
    String uploadFile(@FxFile(value = "image") File file, @FxHeader("token") String token);
```

当参数被@FxFile指定时，参数可以有两种类型，File或byte[]  或您没有在@FxFile中指定filename的值，且参数类型为File那么文件名默认使用file.getName() 或您的参数是byte[]类型则必须指定filename否则抛出异常！



### 2.6 @FxPath注解的使用

该注解同样用在参数上，用于替换restful风格请求路径上的参数！如下:

```java
@FxHttp(value = "/api/exam/pubexam/{page}", method = HttpMethod.POST)
String pubExamInfo(@FxPath("page") Integer page, @FxHeader("token") String token);
```

使用时@FxPath()中指定的值必须为和@FxHttp的value中动态路径的名称一致，如/**/{page}那么FxPath的参数以应为page否则无法完成替换！



### 2.7 返回值映射

框架会根据您定义的返回值进行类型转换后返回给您！

返回值可以是:

**String   InputStream  byte[]    自定义实体类对象！**

值得注意的是:  当您使用自定义实体类对象作为返回值时，或您的实体类包含泛型，则不会成功解析到泛型对象中，此时建议您将泛型设置为Object类型后用框架整合的fatsjson进行二次解析！



### 2.8 更高的扩展性和差异化

在接口方法参数的定义中除了使用注解定义参数外还有另外几种默认参数类型值得注意！

| 参数类型           | 含义                                                         |
| ------------------ | ------------------------------------------------------------ |
| Map<String,Object> | 表单参数，当您的方法含多个参数时，可以定义参数为该类型，进行多参数传递 |
| Map<String,String> | 请求头信息，当您的方法含多个动态请求头时，可以使用map进行多请求头传递 |
| String             | 当您的方法该String类型的参数，且未用注解标注时，认为该参数是请求体信息 |
| byte[]             | 当您的方法该byte[]类型的参数, 且为用@FxFile标注时，认为该参数是请求体 |



差异化参数，即在您选择不同的http框架时，okhttp3或hutool-http，提供了不同的参数类型或回调

> 当使用的是OkHttp， 方法参数类型还可以是

| 参数类型       | 含义                                                         |
| -------------- | ------------------------------------------------------------ |
| OkHttpClient   | Http请求发送对象，若您想自定义该参数，则必须放在方法的第一个参数上！ |
| RequestBody    | 请求体对象                                                   |
| Callback       | 异步回调对象，当参数中包含此参数，方法默认使用异步发送，且callback对象为异步回调 |
| FxHttpCallback | 应为Callback接口中有两个方法，不利于lambda表达式的书写，所以，提供了该接口，接口中只有一个方法，三个参数: 1.result  是否成功  2.call okhttp-Call对象 3.response Response对象 |

**返回值还可以是Response对象**



> 当使用的是Hutool-Http， 方法参数类型还可以是

FxHttpConsumer接口，您可以如以下方法使用，自定义对框架包装的HttpRequest对象做更高程度的扩展

```java
// 接口方法中
@FxHttp(value = "/api/exam/pubexam", method = HttpMethod.POST)
String pubExamInfo(FxHttpConsumer consume, @FxHeader("token") String token);

// 使用时
pubExamInfo(request->{
    // 次数的request对象就是HttpRequest对象，您可以更高程度的使用该请求对象
}, token)
```

**返回值还可以是HttpResponse对象**



## 3.更新日志



#### v 0.1.1更新日志@FxFilename

增加@FxFilename注解，使得在传输文件时，可以在参数中动态传送文件名！

```java
@FxHttp(url = "https://img.coolcr.cn/api/upload",method = HttpMethod.POST)
    String uploadFile(@FxFile(value = "image") File file, @FxFilename String filename);
```

