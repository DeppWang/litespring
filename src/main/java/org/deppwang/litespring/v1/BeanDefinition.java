package org.deppwang.litespring.v1;

/**
 * Bean 定义类：不存放 Bean，存放 Bean 的信息，如 bean 的 id、className 等
 */
public class BeanDefinition {
    // id 是 beanName（类名）
    private String id;

    // class 路径
    private String beanClassName;

    public BeanDefinition(String id, String beanClassName) {
        this.id = id;
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return this.beanClassName;
    }
}
