## Feign-hystrix-mocker

本地开发需要依赖别人的服务时候，如果不想也把依赖服务启动起来，可以对自己项目中的feign client进行mock，这个工具支持直接在feign client接口上对返回值进行mock，不需要在fallback中编写负责冗长的代码来实现。

##### 原理

Feign mocker在feign开源项目[feign](https://github.com/OpenFeign/feign)中[feign-hystrix](https://github.com/OpenFeign/feign/tree/master/hystrix)模块功能的基础上，对fallback进行了增强，即对FallbackFactory.Default进行封装，当项目启动构建初始化FallbackFactory时候，会根据feign client是否定义@Mock选择是否使用EnhancedFallbackFactory，以替换FallbackFactory默认的实现FallbackFactory.Default. 如远程调用失败则会进行返回值mock；如果调用成功，则不会进入任何一个FallbackFactory.

##### 使用方法

只需要在远程调用接口上使用@Mock注解，@Mapping注解配置类中字段的值。type是类型，name是在type类型中的字段，值是value。value格式不匹配会抛出异常，这里需要用户保证正确性，代码不做判断。

```java
@Mock(mappings = {
        @Mapping(type = Response.class, name = "code", value = "200")
})
@GetMapping("/getPeople")
Response<People> getPeople();

@Mock(mappings = {
        @Mapping(type = Response.class, name = "code", value = "200"),
        @Mapping(type = People.class, name = "name", value = "mock"),
        @Mapping(type = People.class, name = "age", value = "24"),
        @Mapping(type = Address.class, name = "district", value = "hangzhou"),
        @Mapping(type = Address.class, name = "code", value = "0571"),
})
@GetMapping("/getPeopleEx")
Response<People> getPeopleEx();
```

支持的返回值类型

1. 范型，比如rpc常用的封装的返回类型 Response<T>, Response的定义如下

```java
@Data
public class Response<T> {

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应描述
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;
}
```

2. 自定义类

```java
@Data
public class People {
    private String name;
    private Integer age;
    private List<Order> orderList;
    public People() {
    }
    public People(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
```



说明：这里的T不支持java原生类，如Integer, Double等，必须为自定义类

```java
/**
 * T: 不支持 Java包装类型的类，导致返回的data并不是mock的200，而是默认值9
 *
 * @return
 */
@Mock(mappings = {
        @Mapping(type = Response.class, name = "code", value = "200"),
        @Mapping(type = Integer.class, name = "data", value = "200")
})
@GetMapping("/getPeopleEx")
Response<Integer> getInteger();
```

返回验证
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "code": 200,
            "msg": "mockString",
            "data": {
                "name": "mock",
                "age": 24,
                "orderList": [
                    {
                        "orderId": 9,
                        "orderName": "mockString",
                        "addressList": [
                            {
                                "district": "hangzhou",
                                "code": "0571"
                            },
                            {
                                "district": "hangzhou",
                                "code": "0571"
                            }
                        ]
                    },
                    {
                        "orderId": 9,
                        "orderName": "mockString",
                        "addressList": [
                            {
                                "district": "hangzhou",
                                "code": "0571"
                            },
                            {
                                "district": "hangzhou",
                                "code": "0571"
                            }
                        ]
                    }
                ]
            }
        },
        {
            "code": 200,
            "msg": "mockString",
            "data": 9
        }
    ]
}{
  "code": 200,
  "msg": "mockString",
  "data": {
    "list": [
      {
        "id": 9,
        "workNo": "mockString"
      }
    ]
  }
}
```
暂不支持返回值中存在循环依赖。如果有，会抛出异常。

详细使用方法可以参考这个仓库：https://github.com/markytsai/mock-test