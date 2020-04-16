package org.deppwang.litespring.v1;

/**
 * Bean 定义类：不存放 Bean，存放 Bean 的信息，如 bean 的 id、className 等
 */
public class BeanDefinition {
    // id 是 beanName（类名）
    private String id;

    // 全限定名。利用反射，可通过全限定名生成 Bean 实例
    private String beanClassName;

    public BeanDefinition(String id, String beanClassName) {
        this.id = id;
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return this.beanClassName;
    }
}
