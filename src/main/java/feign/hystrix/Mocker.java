package feign.hystrix;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @description:
 * @author: caizhenya
 * @email: caizhenya@shandiantech.com
 * @date: 2021/3/16 20:11
 */
public class Mocker {

  Logger log = LoggerFactory.getLogger(Mocker.class);
  private List<Class<?>> mightRecursiveClasses = Lists.newArrayList();

  private boolean isJavaClass(Class<?> clz) {
    return clz != null && clz.getClassLoader() == null;
  }

  private boolean checkHasCircularDependency(Field field) {
    if (isJavaClass(field.getType())) {
      return false;
    }
    if (mightRecursiveClasses.contains(field.getType())) {
      throw new MockException(field.getDeclaringClass().getCanonicalName()
          + " has circular dependencies. "
          + "Probably refer to field " + field.getName() + "?");
    }
    mightRecursiveClasses.add(field.getType());
    return false;
  }

  public Object processGenericList(Object target,
                                   Field declaredField,
                                   Type actualTypeArgument,
                                   List<Object> mockList,
                                   Type argument)
      throws Exception {
    Type fieldGenericType = declaredField.getGenericType();
    Object listObj;

    // List<Integer> List<String>
    listObj = ReflectUtil.generateWrapperUsingPrimitive(actualTypeArgument);
    if (listObj != null) {
      mockList.add(listObj);
      declaredField.set(target, mockList);
      return listObj;
    }

    // List<List<String>>
    if (actualTypeArgument instanceof ParameterizedType) {
      Type actualTypeArgument1 =
          ((ParameterizedTypeImpl) fieldGenericType).getActualTypeArguments()[0];
      listObj = new ArrayList<>();
      recursive(listObj, fieldGenericType, actualTypeArgument1);
      mockList.add(listObj);
      declaredField.set(target, mockList);
    }

    // List<T>
    if (actualTypeArgument instanceof TypeVariable) {
      // new
      if (fieldGenericType instanceof ParameterizedType) {
        Object obj = ReflectUtil.generateWrapperUsingPrimitive(argument);
        if (obj != null) {
          mockList.add(obj);
          declaredField.set(target, mockList);
          return obj;
        }
        if (argument instanceof Class) {
          listObj = ReflectUtil.generateWrapperUsingPrimitive(argument);
          if (listObj == null) {
            listObj = ((Class<?>) argument).newInstance();
            mockList.add(listObj);
            recursive(listObj, argument, null);
          }
          return listObj;
        }
        Type rawType = ((ParameterizedType) argument).getRawType();
        if (rawType == List.class) {
          listObj = new ArrayList<>();
          processRecursiveList(listObj, argument);
          mockList.add(listObj);
        }
        // if (rawType instanceof Class) {
        // listObj = ((Class) rawType).newInstance();
        // mockList.add(listObj);
        // recursive(listObj, argument, ((ParameterizedTypeImpl)
        // argument).getActualTypeArguments()[0]);
        // }
        declaredField.set(target, mockList);
        return listObj;
      }
      // end

      if (argument instanceof Class) {
        listObj = ReflectUtil.generateWrapperUsingPrimitive(argument);
        if (listObj == null) {
          listObj = ((Class<?>) argument).newInstance();
          mockList.add(listObj);
          recursive(listObj, argument, null);

        }
        mockList.add(listObj);
        declaredField.set(target, mockList);
        return listObj;
      }
    }

    if (actualTypeArgument instanceof Class) {
      listObj = ((Class) actualTypeArgument).newInstance();
      mockList.add(listObj);
      recursive(listObj, actualTypeArgument, null);
    }
    declaredField.set(target, mockList);
    return listObj;
  }

  public Object mockResponse(Method method) throws Exception {
    log.info("mock method [{}]", method.getDeclaringClass().getName() + "." + method.getName());
    try {
      Type returnType = method.getGenericReturnType();
      Object response = null;
      if (returnType instanceof ParameterizedType) {
        Type rawType = ((ParameterizedType) returnType).getRawType();
        if (rawType instanceof Class) {
          Type argument;
          Mock annotation = method.getAnnotation(Mock.class);
          Class<?> type = annotation.generic();
          Mapping[] values = annotation.mappings();
          for (Mapping value : values) {
            if (ReflectUtil.fieldMap.keySet().contains(value.type())) {
              ReflectUtil.fieldMap.get(value.type()).put(value.name(), value.value());
            } else {
              HashMap<String, String> map = new HashMap<>();
              map.put(value.name(), value.value());
              ReflectUtil.fieldMap.put(value.type(), map);
            }
          }
          if (type != void.class && type != Void.class) {
            argument = type;
          } else {
            argument = ((ParameterizedType) returnType).getActualTypeArguments()[0];
          }
          response = ((Class) rawType).newInstance();
          recursive(response, returnType, argument);
        }
      }
      if (returnType instanceof Class) {
        response = ((Class<?>) returnType).newInstance();
        recursive(response, returnType, null);
      }
      return response;
    } catch (Exception e) {
      log.info("Exception: ", e);
      throw e;
    }
  }

  private void processRecursiveList(Object target, Type argument) throws Exception {
    Object listObj = ReflectUtil.generateWrapperUsingPrimitive(argument);
    if (listObj != null) {
      ((List<Object>) target).add(listObj);
      return;
    }
    if (argument instanceof Class) {
      listObj = ((Class<?>) argument).newInstance();
      recursive(listObj, argument, null);
    }
    if (listObj != null) {
      ((List<Object>) target).add(listObj);
      return;
    } else {
      if (argument instanceof ParameterizedType) {
        listObj = new ArrayList<>();
        recursive(listObj, argument, ((ParameterizedType) argument).getActualTypeArguments()[0]);
        ((List<Object>) target).add(listObj);
      }
    }

  }

  public void recursive(Object target, Type targetType, Type argument) throws Exception {

    // start
    if (target instanceof List) {
      processRecursiveList(target, argument);
      return;
    }
    // end

    Field[] declaredFields = target.getClass().getDeclaredFields();
    for (Field declaredField : declaredFields) {
      checkHasCircularDependency(declaredField);
      if (Modifier.isFinal(declaredField.getModifiers())) {
        continue;
      }

      declaredField.setAccessible(true);
      Type fieldGenericType = declaredField.getGenericType();

      if (fieldGenericType != Collection.class && ReflectUtil.setField(declaredField, target)) {
        continue;
      }

      if (fieldGenericType instanceof Class) {
        Object obj = ((Class<?>) fieldGenericType).newInstance();
        declaredField.set(target, obj);
        recursive(obj, declaredField.getGenericType(), null);
      }

      // List<T> List<String> List<Object> List<List<T>> RestPage<T>
      if (fieldGenericType instanceof ParameterizedType) {
        Type fieldRawType = ((ParameterizedType) fieldGenericType).getRawType();
        List mockList = new ArrayList();

        // List<T> List<String> List<Object> List<List<T>>
        if (fieldRawType == List.class) {
          Type actualTypeArgument =
              ((ParameterizedType) fieldGenericType).getActualTypeArguments()[0];
          Object listObj =
              processGenericList(target, declaredField, actualTypeArgument, mockList, argument);
          if (actualTypeArgument instanceof Class) {
            listObj = ReflectUtil.generateWrapperUsingPrimitive(actualTypeArgument);
            if (listObj != null) {
              continue;
            }
            listObj = ((Class) actualTypeArgument).newInstance();
            mockList.add(listObj);
            recursive(listObj, actualTypeArgument, null);
          }
        }
        declaredField.set(target, mockList);
      }

      if (fieldGenericType instanceof TypeVariable) {
        Object obj;
        Type varType = ((ParameterizedType) targetType).getActualTypeArguments()[0];
        if (varType instanceof ParameterizedType) {
          Type rawType = ((ParameterizedType) varType).getRawType();
          List mockList = new ArrayList();
          if (rawType == List.class) {
            Type actualTypeArgument =
                ((ParameterizedTypeImpl) argument).getActualTypeArguments()[0];
            Object listObj =
                processGenericList(target, declaredField, actualTypeArgument, mockList, argument);
            continue;
          }

          if (rawType instanceof Class) {
            obj = ((Class) rawType).newInstance();
            declaredField.set(target, obj);
            recursive(obj, varType, ((ParameterizedTypeImpl) argument).getActualTypeArguments()[0]);
          }
        }

        // List<?>
        if (varType instanceof WildcardType) {
          obj = ReflectUtil.generateWrapperUsingPrimitive(argument);
          if (obj != null) {
            declaredField.set(target, obj);
            continue;
          }
          if (argument instanceof Class) {
            obj = ((Class<?>) argument).newInstance();
            declaredField.set(target, obj);
            recursive(obj, declaredField.getGenericType(), null);
          }
        }

        if (varType instanceof Class) {
          obj = ReflectUtil.generateWrapperUsingPrimitive(argument);
          if (obj == null) {
            obj = ((Class<?>) varType).newInstance();
            declaredField.set(target, obj);
            recursive(obj, targetType, null);
          }
          declaredField.set(target, obj);
        }
      }
      mightRecursiveClasses.remove(declaredField.getType());
    }
  }
}
