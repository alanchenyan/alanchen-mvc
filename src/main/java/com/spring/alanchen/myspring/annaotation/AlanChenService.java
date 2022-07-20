package com.spring.alanchen.myspring.annaotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE}) // 该注解用在类上
@Retention(RetentionPolicy.RUNTIME) // 运行时可通过反射机制获取该注解
@Documented // 该注解包含到javadoc中

public @interface AlanChenService {

    String value() default "";
}
