package org.deppwang.litespring.v4;

/**
 * Bean 定义类：不存放 Bean，存放 Bean 的信息，如 bean 的 id、className 等
 */
public class BeanDefinition {
    // id 是 beanName（类名）
    private String id;

    // 全限定名
    private String beanClassName;

    private boolean singleton = true;

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
}