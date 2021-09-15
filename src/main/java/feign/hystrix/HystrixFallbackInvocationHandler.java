package feign.hystrix;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @description:
 * @author: caizhenya
 * @email: caizhenya@shandiantech.com
 * @date: 2021/3/16 20:12
 */
public class HystrixFallbackInvocationHandler implements InvocationHandler {

  private Object api;
  private Class<?> type;
  private Map<Method, Boolean> mockMap;
  private Mocker mocker = new Mocker();

  public <T> T proxy() {
    return (T) Proxy.newProxyInstance(type.getClassLoader(),
        type.getInterfaces(), this);
  }

  public HystrixFallbackInvocationHandler(Object api, Map<Method, Boolean> mockMap) {
    this.api = api;
    this.mockMap = mockMap;
    this.type = api.getClass();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return mockMap.size() != 0 && mockMap.get(method) ? mocker.mockResponse(method)
        : method.invoke(api, args);
  }
}
