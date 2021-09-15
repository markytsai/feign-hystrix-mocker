package feign.hystrix;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: caizhenya
 * @email: caizhenya@shandiantech.com
 * @date: 2021/3/17 12:39
 */
public class ReflectUtil {

  public static Map<Class<?>, Map<String, String>> fieldMap = Maps.newHashMap(new HashMap<>());

  public static boolean setField(Field field, Object ret) throws Exception {
    Class type = field.getType();
    Class ownerType = field.getDeclaringClass();
    String name = field.getName();
    String value = "";
    boolean isFieldCustomed = false;
    if (fieldMap.keySet().contains(ownerType) && fieldMap.get(ownerType).keySet().contains(name)) {
      isFieldCustomed = true;
      value = fieldMap.get(ownerType).get(name);
    }

    boolean isPrimitive = false;
    if (type == int.class) {
      if (isFieldCustomed) {
        field.set(ret, Integer.valueOf(value));
      } else {
        field.set(ret, 999);
      }
      isPrimitive = true;
    }
    if (type == double.class) {
      if (isFieldCustomed) {
        field.set(ret, Double.valueOf(value));
      } else {
        field.set(ret, 999.9);
      }
      isPrimitive = true;
    }
    if (type == boolean.class) {
      if (isFieldCustomed) {
        field.set(ret, Boolean.valueOf(value));
      } else {
        field.set(ret, Boolean.TRUE);
      }
      isPrimitive = true;
    }
    if (type == char.class) {
      if (isFieldCustomed) {
        field.set(ret, Character.valueOf(value.charAt(0)));
      } else {
        field.set(ret, '0');
      }
      isPrimitive = true;
    }
    if (type == long.class) {
      if (isFieldCustomed) {
        field.set(ret, Long.valueOf(value));
      } else {
        field.set(ret, 999L);
      }
      isPrimitive = true;
    }
    if (type == float.class) {
      if (isFieldCustomed) {
        field.set(ret, Float.valueOf(value));
      } else {
        field.set(ret, 9.99f);
      }
      isPrimitive = true;
    }
    if (type == byte.class) {
      if (isFieldCustomed) {
        field.set(ret, Byte.valueOf(value));
      } else {
        field.set(ret, (byte) 9);
      }
      isPrimitive = true;
    }
    if (type == short.class) {
      if (isFieldCustomed) {
        field.set(ret, Short.valueOf(value));
      } else {
        field.set(ret, (short) 9);
      }
      isPrimitive = true;
    }

    if (type == Integer.class) {
      if (isFieldCustomed) {
        field.set(ret, Integer.valueOf(value));
      } else {
        field.set(ret, 9);
      }
      isPrimitive = true;
    }
    if (type == Double.class) {
      if (isFieldCustomed) {
        field.set(ret, Double.valueOf(value));
      } else {
        field.set(ret, 999.9);
      }
      isPrimitive = true;

    }
    if (type == Boolean.class) {
      if (isFieldCustomed) {
        field.set(ret, Boolean.valueOf(value));
      } else {
        field.set(ret, Boolean.TRUE);
      }
      isPrimitive = true;

    }
    if (type == Character.class) {
      if (isFieldCustomed) {
        field.set(ret, Character.valueOf(value.charAt(0)));
      } else {
        field.set(ret, '0');
      }
      isPrimitive = true;

    }
    if (type == Long.class) {
      if (isFieldCustomed) {
        field.set(ret, Long.valueOf(value));
      } else {
        field.set(ret, 999L);
      }
      isPrimitive = true;

    }
    if (type == Float.class) {
      if (isFieldCustomed) {
        field.set(ret, Float.valueOf(value));
      } else {
        field.set(ret, 9.99f);
      }
      isPrimitive = true;

    }
    if (type == Byte.class) {
      if (isFieldCustomed) {
        field.set(ret, Byte.valueOf(value));
      } else {
        field.set(ret, (byte) 9);
      }
      isPrimitive = true;

    }
    if (type == Short.class) {
      if (isFieldCustomed) {
        field.set(ret, Short.valueOf(value));
      } else {
        field.set(ret, (short) 9);
      }
      isPrimitive = true;

    }
    if (type == String.class) {
      if (isFieldCustomed) {
        field.set(ret, value);
      } else {
        field.set(ret, "mockString");
      }
      isPrimitive = true;

    }
    if (type == Date.class) {
      if (isFieldCustomed) {
        field.set(ret, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(value));
      } else {
        field.set(ret, new Date());
      }
      isPrimitive = true;
    }
    if (type == LocalDate.class) {
      if (isFieldCustomed) {
        field.set(ret,
            LocalDateTime.ofInstant(new SimpleDateFormat("yyyy-MM-dd").parse(value).toInstant(),
                ZoneId.systemDefault()).toLocalDate());
      } else {
        field.set(ret, LocalDate.now());
      }
      isPrimitive = true;
    }
    if (type == LocalDateTime.class) {
      if (isFieldCustomed) {
        field.set(ret,
            LocalDateTime.ofInstant(
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(value).toInstant(),
                ZoneId.systemDefault()));
      } else {
        field.set(ret, LocalDateTime.now());
      }
      isPrimitive = true;
    }

    return isPrimitive;
  }

  public static Object generateWrapperUsingPrimitive(Type type) {

    if (type == Integer.class) {
      return 9;
    }
    if (type == Double.class) {
      return 999.9;
    }
    if (type == Boolean.class) {
      return Boolean.TRUE;
    }
    if (type == Character.class) {
      return '0';
    }
    if (type == Long.class) {
      return 999L;
    }
    if (type == Float.class) {
      return 9.99f;
    }
    if (type == Byte.class) {
      return (byte) 9;
    }
    if (type == Short.class) {
      return (short) 9;
    }
    if (type == String.class) {
      return "mockString";
    }
    if (type == Date.class) {
      return new Date();
    }
    if (type == LocalDate.class) {
      return LocalDate.now();
    }
    if (type == LocalDateTime.class) {
      return LocalDateTime.now();
    }
    return null;
  }
}
