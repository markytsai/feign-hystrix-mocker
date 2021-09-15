package feign.hystrix;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: caizhenya
 * @email: caizhenya@shandiantech.com
 * @date: 2021/3/20 11:37
 */
class EnhancedFallbackFactory<T> extends FallbackFactory.Default<T> {
  private Map<Method, Boolean> mockMap = new HashMap<>();
  private FallbackFactory<T> delegateFallbackFactory;

  public EnhancedFallbackFactory(FallbackFactory fallbackFactory) {
    // 这句其实没有具体作用，只是为了照顾语法
    super();
    delegateFallbackFactory = fallbackFactory;
  }

  @Override
  public Map<Method, Boolean> getMockMap() {
    return mockMap;
  }

  public EnhancedFallbackFactory(T constant) {
    super(constant);
    Class<?>[] interfaces = constant.getClass().getInterfaces();
    Class<?> interfaceOnlyOne = interfaces[0];
    Method[] declaredMethods = interfaceOnlyOne.getDeclaredMethods();
    for (Method declaredMethod : declaredMethods) {
      if (declaredMethod.isAnnotationPresent(Mock.class)) {
        mockMap.put(declaredMethod, Boolean.TRUE);
      } else {
        mockMap.put(declaredMethod, Boolean.FALSE);
      }
    }
  }

  @Override
  public T create(Throwable cause) {
    if (delegateFallbackFactory == null) {
      return super.get();
    }
    T t = delegateFallbackFactory.create(cause);
    Object fallbackProxy =
        new HystrixFallbackInvocationHandler(new EnhancedFallbackFactory<>(t).get(),
            new EnhancedFallbackFactory<>(t).getMockMap()).proxy();
    return (T) fallbackProxy;
  }
}
