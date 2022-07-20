package com.spring.alanchen.myspring.annaotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD}) // 该注解用在类的成员变量上
@Retention(RetentionPolicy.RUNTIME) // 运行时可通过反射机制获取该注解
@Documented // 该注解包含到javadoc中

public @interface AlanChenAutowired {

    String value() default "";
}
