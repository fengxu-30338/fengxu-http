风珝Http映射工具



## 1.添加依赖

```kotlin
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    implementation 'com.github.fengxu-30338:fengxu-http:0.2.2'
    
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

| @FxHttp属性    | 含义                                                         |
| -------------- | :----------------------------------------------------------- |
| value          | 配合baseUrl真正的url为 baseUrl + FxHttp.value()              |
| url            | 或您想单独为该方法设置url只需要设置其url属性即可如@Fxhttp(url="http://xx.xx ")，同时该注解中的value属性，也就失效了，不在配合baseUrl做为真实发送的url |
| timeout        | 请求的超时时间，默认3000包括(连接超时和读取超时)             |
| connectTimeout | 连接超时时间，设置了该项会覆盖timeout                        |
| readTimeout    | 读取超时时间，设置了该项会覆盖timeout                        |
| method         | http方法类型(枚举值)如 GET,POST,PUT ...                      |
| headers        | 固定请求头用" : "隔开如 {"token:asdffdg","X-Requested-With:XMLHttpRequest"} |
| throwable      | 发送请求或解析结果出错时，是否抛出异常,默认true,设置false则错误时，返回值为null |
| patterMore     | 是在设置多个拦截器时，能否同时匹配多个，默认false            |





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

注：在v0.1.1版本以后可以配合@FxFilename指定文件的文件名！



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
| String             | 当您的方法该String类型的参数，且未用注解标注时，认为该参数是json请求体信息 |
| byte[]             | 当您的方法该byte[]类型的参数, 且未用@FxFile标注时，认为该参数是请求体 |



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



#### v0.1.1更新日志@FxFilename

增加@FxFilename注解，使得在传输文件时，可以在参数中动态传送文件名！

```java
@FxHttp(url = "https://img.coolcr.cn/api/upload",method = HttpMethod.POST)
    String uploadFile(@FxFile(value = "image") File file, @FxFilename String filename);
```



#### v0.2.0更新日志-添加拦截器

用户可以在构建代理对象时，设置拦截器，拦截器匹配@FxHttp注解中的value属性，也就是如果您是以url的形式指定请求路径的，那么匹配规则将失效，但可以通过同时存在url和value属性时来使其能够被拦截器匹配到，因为您设置了url属性后，将不会使用value属性作为请求路径！

```java
FxTest fxTest = new FxHttpMain.Builder().startLog(true)
    			// 设置拦截器
                .setInterceptor(fxHttpInterceptor -> {
                    // 添加匹配规则（正则表达式）
                    fxHttpInterceptor.addPattern("/api/*")
                            // 为所有匹配规则的请求都增加表单参数项
                            .addForm("test","测试数据")
                        	// 为所有匹配规则的请求都增加请求头参数
                            .addHeader("token","token");
                })
                .build(FxTest.class);
        String s = fxTest.login("风珝", "123321");
        System.out.println(s);
```



#### v0.2.1 更新日志-更新拦截器

更新了拦截器可以获取到请求方法的参数和请求头，但是只有在匹配到时，对其作出的改变才会应用到请求上，也就是每次拦截器中都能拿到请求参数的副本，只有在正确匹配时，该副本才会应用到原来的请求上！

其次，修复了不影响使用的小bug,优化了代码！





#### v0.2.2 更新日志-多拦截器匹配

在@FxHttp注解中增加了patterMore属性，如果您设置了多个拦截器。默认是匹配到最先设置的拦截器，若您想匹配多个拦截器，则将patterMore设置为true即可匹配多个拦截器！

```java
// 接口方法中
@FxHttp(value = "/api/user/login",method = HttpMethod.POST,timeout = 5000,patterMore = true)
    String login(@FxQuery("username") String user,@FxQuery("password")  String password);

// 调用中
FxTest fxTest = new FxHttpMain.Builder()
                .baseUrl("http://8.131.71.175")
                .startLog(true)
    			// 设置第一项拦截器
                .addInterceptor(fxHttpInterceptor -> {
                    fxHttpInterceptor.addHeader("token","123324");
                    System.out.println(fxHttpInterceptor.getForm());
                },"/pub")
    			// 设置第二项拦截器
                .addInterceptor(fxHttpInterceptor -> {
                    fxHttpInterceptor.addHeader("token2","qwwqewqe")
                            .addForm("sign","b3c159af4edc4850bcb64fbab32d89df");
                },"/api")
                .build(FxTest.class);
        String s = fxTest.login("风珝", "123321");
```

