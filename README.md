# feign-mock

An original readme refers to [README.md](README-origin.md)

Feign Mock在feign开源项目中feign-hystrix模块功能的基础上，对fallback进行了增强，对远程调用方法返回值进行mock，便于测试和本地业务验证。

只需要在远程调用接口上使用@Mock注解，
```java
public interface RiskMicroClient {

  @Mock
  @RequestLine("GET /test")
  RestResponse<RestPage<ComplaintHandlerMicroResponse>> queryComplaintHandlerList();
}
```

```java
RiskMicroClient riskFallback = () -> {
      List<ComplaintHandlerMicroResponse> responses =
              Arrays.asList(new ComplaintHandlerMicroResponse());
      RestPage restPage = new RestPage(responses);
      return new RestResponse<>("this is fallback", restPage);
    };
RiskMicroClient client = target(RiskMicroClient.class, "http://localhost:" + server.getPort(), riskFallback);
RestResponse<RestPage<ComplaintHandlerMicroResponse>> restPageRestResponse = client.queryComplaintHandlerList();
Gson niceGson = new GsonBuilder().setPrettyPrinting().create();
System.out.println(niceGson.toJson(restPageRestResponse));
```

fallback增强后，feign抛出异常后，hystrix捕获异常，根据接口是否有@Mock注解判断是否对返回值mock。进过mock的接口如下：

```json
{
  "code": 999,
  "msg": "mockString",
  "data": {
    "list": [
      {
        "id": 9,
        "workNo": "mockString",
        "providerNo": "mockString",
        "providerName": "mockString",
        "workOrderType": "mockString",
        "workOrderTypeShow": "mockString",
        "cityCode": "mockString",
        "cityName": "mockString",
        "brandNo": "mockString",
        "brandName": "mockString",
        "driverName": "mockString",
        "driverPhone": "mockString",
        "driverNo": "mockString",
        "orderNo": "mockString",
        "payState": 9,
        "payStateShow": "mockString",
        "needReparation": 9,
        "needReparationShow": "mockString",
        "needCallback": 9,
        "needCallbackShow": "mockString",
        "workOrderReason": "mockString",
        "handleContext": "mockString",
        "handlePerson": "mockString",
        "handleState": "mockString",
        "handleStateShow": "mockString",
        "handleTime": "mockString",
        "handleReason": "mockString",
        "lastOperatorId": 9,
        "lastOperatorName": "mockString",
        "createPerson": "mockString",
        "bizDate": "mockString",
        "lastOperatorTime": "mockString",
        "cancelDetails": "mockString",
        "cancelReasons": "mockString",
        "cancelPersons": "mockString"
      }
    ]
  }
}
```

如果希望定义某个字段为自定义值，使得后面的业务逻辑可以持续而不会中断，可以使用@Mapping注解配置类中字段的值。type是类型，name是在type类型中的字段（暂时只支持class from jdk），值是value。value格式不匹配会抛出异常，这里需要用户保证正确性，代码不做判断。
```java
  @Mock(
          mappings = {
                  @Mapping(type = RestResponse.class, name = "code", value = "200"),
                  @Mapping(type = RestResponse.class, name = "msg", value = "success")
          }
  )
  @RequestLine("GET /test")
  RestResponse<RestPage<ComplaintHandlerMicroResponse>> queryComplaintHandlerList();
```

返回对象
```json
{
  "code": 200,
  "msg": "mockString",
  "data": {
    "list": [
      {
        "id": 9,
        "workNo": "mockString",
        "providerNo": "mockString",
        "providerName": "mockString",
        "workOrderType": "mockString",
        "workOrderTypeShow": "mockString",
        "cityCode": "mockString",
        "cityName": "mockString",
        "brandNo": "mockString",
        "brandName": "mockString",
        "driverName": "mockString",
        "driverPhone": "mockString",
        "driverNo": "mockString",
        "orderNo": "mockString",
        "payState": 9,
        "payStateShow": "mockString",
        "needReparation": 9,
        "needReparationShow": "mockString",
        "needCallback": 9,
        "needCallbackShow": "mockString",
        "workOrderReason": "mockString",
        "handleContext": "mockString",
        "handlePerson": "mockString",
        "handleState": "mockString",
        "handleStateShow": "mockString",
        "handleTime": "mockString",
        "handleReason": "mockString",
        "lastOperatorId": 9,
        "lastOperatorName": "mockString",
        "createPerson": "mockString",
        "bizDate": "mockString",
        "lastOperatorTime": "mockString",
        "cancelDetails": "mockString",
        "cancelReasons": "mockString",
        "cancelPersons": "mockString"
      }
    ]
  }
}
```
暂不支持返回值中存在循环依赖。如果有，会抛出异常。