# 造轮子，实现一个简易的 spring IoC 容器

1. Bean 实例化
2. 填充属性-依赖注入
4. 工厂方法模式与单例模式
4. 实现注解
5. 管理生命周期、实现 AOP
6. ApplicationContext 和 BeanFactory 的区别：一个开始实例化，一个用到时实例化

## 一、实现实例化 Bean 

通过 xml 配置文件实例化 Bean

1. 从 xml 配置文件获取 Bean 信息，如 className 等，使用 BeanDefinition 存放
2. IoC 容器本质是一个 Map，Map 存放所有 BeanDefinition
3. 根据 BeanDefinition 的 className，利用反射生成 Bean，此时 bean 没有属性

从配置文件中，根据 bean 指定的类全限定名，生成 bean，注入容器

## 二、实现填充属性（依赖注入）

bean 配置的 property 绑定 setter 方法，通过 setter 方法实现依赖注入。还可以构造器注入。

如果此时 Bean 引用其他实体，实例化的 bean 只有引用，而没有对象。

![image-20200414204030815](/Users/yanjie/Library/Application Support/typora-user-images/image-20200414204030815.png)

![img](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-03-24-140232.png)

```
public class BeanDefinition {
    private String id;
    private String beanClassName;

    List<String> propertyValues = new ArrayList<>();
}
```

依赖注入（注入实例）

```
Method.invoke(bean, propertyBean);
```

spring 类加载，创建实例过程中，需要

## 三、利用工厂方法模式和单例模式

单例模式：

- 使用 map 存放唯一 bean

工厂模式

- 不直接 new，使用简单工厂 new
- 实现一个接口，可有不同的实现。

简单工厂模式

- >*定义一个用于创建对象的接口，让子类决定实例化哪一个类。Factory Method 使一个类的实例化延迟到其子类。*

## 三、实现注解

包扫描+注解

## 四、SpringBoot

@ComponentScan 替代 applicationContext.xml 的 `<component-scan>`

@Configuration 相当于配置文件，如 shiro.xml

@EnableAutoConfiguration 相当于在 applicationContext.xml 加入配置

引入 jar 包，需要将其注入容器，将 spring.factories 的 Configuration 加入容器，Configuration 执行 @ComponentScan

在 SpringMVC，引入 jar 的包如何注入容器？

通过配置文件：

```xml
	<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
		<property name="maxIdle" value="300" /> <!-- 最大能够保持idel状态的对象数 -->
		<property name="maxTotal" value="60000" /> <!-- 最大分配的对象数 -->
		<property name="testOnBorrow" value="true" /> <!-- 当调用borrow Object方法时，是否进行有效性检查 -->
	</bean>
```

- Application 扫描 xml 的 bean，将 JedisPoolConfig 注入容器，

jdk jar 包、工具 jar 包的类是否需要注入容器？

- 容器只是为了管理业务类，注入容器的类都有 @Component 注解。

多个请求并发访问时，是否有多个容器？

- 容器是对象属性，线程共享，多个线程可同时操作，为了保证线程安全，用 final 修饰。



