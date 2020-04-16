package org.deppwang.litespring.v3;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean 定义类：不存放 Bean，存放 Bean 的信息，如 bean 的 id、className 等
 */
public class BeanDefinition {
    // id 是 beanName（类名）
    private String id;

    // 全限定名
    private String beanClassName;

    // 除了使用单例创建，还可以使用原型创建，默认使用单例创建
    private boolean singleton = true;

    // 存放 Bean 属性名称
    List<String> propertyNames = new ArrayList<>();

    public BeanDefinition(String id, String beanClassName) {
        this.id = id;
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {

        return this.beanClassName;
    }

    public boolean isSingleton() {
        return this.singleton;
    }

    public List<String> getPropertyNames() {
        return this.propertyNames;
    }


}