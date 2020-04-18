package org.deppwang.litespring.v4.stereotype;

import org.springframework.context.annotation.ComponentScans;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
//@Repeatable(ComponentScans.class)
public @interface ComponentScan {
    String[] value() default {};
}
