package org.deppwang.litespring.v4.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 目标：代表注解用到什么地方。Element 的类型，TYPE 为 Class Interface 等等
@Retention(RetentionPolicy.RUNTIME) // 保留策略：运行时。代表注解在运行时也被保留
@Documented // 将此注解包含到 Javadoc 中
public @interface Component {
    String value() default ""; // value 相当于 Component 属性，默认为 ""
}
