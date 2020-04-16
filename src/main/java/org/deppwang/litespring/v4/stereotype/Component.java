package org.deppwang.litespring.v4.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 目标：Element 的类型，TYPE 为 Class Interface 等等
@Retention(RetentionPolicy.RUNTIME) // 保留策略：运行时
@Documented // 已记录
public @interface Component {
    String value() default ""; // value 相当于 Component 属性，默认为 ""
}
