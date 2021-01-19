package tech.punklu.coupon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略统一响应注解定义
 */
@Target({ElementType.TYPE,ElementType.METHOD}) // 可以标识在类上，方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
public @interface IgnoreResponseAdvice {
}
