package org.deppwang.litespring.v4;


//import org.deppwang.litespring.v4.stereotype.ComponentScan;
//import org.deppwang.litespring.v4.stereotype.Configuration;

import org.deppwang.litespring.v4.service.PetStoreService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan // ComponentScan 扫描 PetStoreConfig 所在路径及其所在路径所有子路径
public class PetStoreConfig {
    public static void main(String[] args) {
        // 当使用 Spring 框架的 AnnotationConfigApplicationContext 时，所有注解需要修改为原生注解，因为我们自定义注解它不能识别
        ApplicationContext context = new AnnotationConfigApplicationContext(PetStoreConfig.class);
        PetStoreService userService = context.getBean(PetStoreService.class);
        userService.getAccountDao();
    }
}