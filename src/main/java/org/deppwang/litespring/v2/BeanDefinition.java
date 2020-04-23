package org.deppwang.litespring.v2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Bean 定义类：不存放 Bean，存放 Bean 的信息，如 bean 的 id、className 等
 */
public class BeanDefinition {
    private String id;

    private String beanClassName;

    // 存放 Bean 的所有属性名称，用于获取 Bean
    private final List<String> propertyNames = new ArrayList<>();

    private final List<String> constructorArguments = new LinkedList<>();

    public BeanDefinition(String id, String beanClassName) {
        this.id = id;
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return this.beanClassName;
    }

    public List<String> getPropertyNames() {
        return this.propertyNames;
    }

    public List<String> getConstructorArgumentValues() {
        return this.constructorArguments;
    }

    boolean hasConstructorArgumentValues() {
        return !this.constructorArguments.isEmpty();
    }
}