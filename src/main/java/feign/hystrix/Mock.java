package feign.hystrix;

import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @description:
 * @author: caizhenya
 * @email: caizhenya@shandiantech.com
 * @date: 2021/3/16 20:11
 */
@java.lang.annotation.Target(METHOD)
@Retention(RUNTIME)
public @interface Mock {

  Class<?> generic() default void.class;

  Mapping[] mappings() default {};
}
