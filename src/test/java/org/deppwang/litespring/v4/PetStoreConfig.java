package org.deppwang.litespring.v4;


//import org.deppwang.litespring.v4.stereotype.ComponentScan;
//import org.deppwang.litespring.v4.stereotype.Configuration;

import org.deppwang.litespring.v4.service.PetStoreService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration // 将类标记为 @Configuration，代表这个类是相当于一个配置文件
@ComponentScan // ComponentScan 扫描 PetStoreConfig.class 所在路径及其所在路径所有子路径的文件
public class PetStoreConfig {
}