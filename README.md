# 造轮子：实现一个简易的 Spring IoC 容器

我通过实现一个简易的 Spring IoC 容器，算是入门了 Spring 框架。本文是对实现过程的一个总结提炼，**需要配合源码阅读**，[源码地址](https://github.com/DeppWang/litespring)。

结合本文和源码，你应该可以学到：Spring 的原理和 Spring Boot 的原理

Spring 框架是 Java 开发的，Java 是面向对象的语言，所以 Spring 框架本身有大量的抽象、继承、多态。对于初学者来说，光是理清他们的逻辑就很麻烦，我摒弃了哪些包装，只实现了最本质（核心）的功能。代码不是很严谨，但只为了理解 Spring 思想却够了。

<!--代码不是很严谨，但我们主要是为了理解其思想，而不是真正实现一个完美无缺的框架。-->

下面正文开始。

---

## 零、前言

在没有 Spring 框架的远古时代，我们业务逻辑长这样：

```Java
public class PetStoreService {
    AccountDao accountDao = new AccountDao();
}

public class AccountDao {
}

PetStoreService petStoreService = new PetStoreService();
```

到处都是  **new** 关键字，需要开发人员显式的使用 **new** 关键字来创建业务类对象。这样有很多弊端，如，创建的对象太多，耦合性太强，等等。

有个叫 [Rod Johnson](https://en.wikipedia.org/wiki/Rod_Johnson_(programmer)) 老哥对此很不爽，就开发了一个叫 Spring 的框架，就是为了干掉 new 关键字（哈哈，我杜撰的，只是为了说明 Spring 的作用）。

有了 Spring 框架，由框架来新建对象，管理对象，并处理对象之间的依赖，我们程序员再也不用 new 业务类对象了。我们来看看框架是如何实现的吧。

本节源码对应：**v0**

## 一、实例化 Bean 

首先，Spring 框架需要实例化类，将其转换为对象。在 Spring 框架中，我们管类叫 Bean，所以也可以说实例化 Bean。

早期 Spring 框架需要借助 xml 配置文件来实现实例化 Bean，可以分为三步（配合源码 **v1** 阅读）：

1. 从 xml 配置文件获取 Bean 信息，如全限定名等，作为 BeanDefinition（Bean 定义类）的属性
2. 使用一个 Map 存放所有 BeanDefinition，此时 IoC 容器本质上一个 Map，存放 BeanDefinition
3. 当获取 Bean 实例时，通过类加载器，根据全限定名，得到其类对象，通过类对象利用反射创建 Bean 实例

这里有两个重要的知识点：1、类加载；2、反射。前者可以看看[《深入理解 Java 虚拟机》](https://book.douban.com/subject/34907497/)第 7 章，后者可以看看《廖雪峰 Java 教程》[反射](https://www.liaoxuefeng.com/wiki/1252599548343744/1255945147512512) 部分。

名词解释：

- Bean：在 Spring 中，Bean 一般也被用来对象，所有
-  IoC 容器：
- 全限定名：指编译后的 class 文件所有的在 jar 包中的路径

源码对应：**v1**

## 二、填充属性（依赖注入）

当实例化 Bean 后，当 Bean 对象有成员变量（属性）时，因为没有指向实例化对象，所以为 null。

![image-20200417223302844](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-17-143303.png)

此时需要通过一种方式实现，让 accountDao 引用指向实例，我们管这一步叫填充属性。

当一个 Bean 的成员变量类型是另一个 Bean 时，我们可以说一个 Bean 依赖于另一个 Bean。所以填充属性，也可以称为依赖注入（**D**ependency **I**njection）。

抛开 Spring 不谈，在正常情况下，我们有两种方式实现依赖注入，1、使用 Setter 方法，2、使用构造方法。使用 Setter 方法如下：

```Java
public class PetStoreService {
    private AccountDao accountDao;
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }
}

public class AccountDao {
}

PetStoreService petStore = new PetStoreService();
petStore.setAccountDao(new AccountDao()); // 将依赖 new AccountDao() 注入 petStore
```

其实早期 Spring 框架也是通过这两种方式来实现依赖注入的。下面是 Spring 框架通过 xml 文件 + Setter 来实现依赖注入的步骤（配合源码 **v2** 阅读）：

1. 给 PetStoreService 添加 setter 方法，并稍微修改一下 xml 配置文件，添加 `<property>`，代表对应 setter 方法。
2. 从 xml 配置文件获取 Bean 的属性 `<property>`，存放到 BeanDefinition 的 propertyNames 中。
3. 利用 PropertyDescriptor 的 Method.invoke()（反射），通过 setter 方法将 bean 的属性关联到对象实例

基于构造函数实现依赖注入的方式跟 Setter 方法差不多，感兴趣可以 Google 搜索查看。

因为 Spring 框架实现了依赖注入，所以我们程序员没有了创建对象的控制权，所以也被称为控制反转。

源码对应：**v2**

## 三、单例模式、工厂方法模式

前面两步实现了获取 Bean 对象时创建 Bean，但 Bean 经常使用，不能每次都新创建，Spring 框架其实一个 Bean 只对应一个对象，这需要使用单例模式。

> 单例模式：每个类有且只有一个实例

正常单例模式通过类或者枚举生成对象，Spring 使用类对象创建 Bean，两者不太一样。

Spring 其实使用一个 Map 存放所有 Bean。创建时，先看 Map 中是否有 Bean，没有就创建；获取时，直接从 Map 中获取。这种方式能保证一个类只有一个 Bean。

```Java
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(64);
```

早期 Spring 框架使用 Bean 的策略是用到时再实例化所用 Bean，杰出代表是 XmlBeanFactory，后期为了实现更多的功能，创建了 ApplicationContext，两者都继承与 BeanFactory 接口。这使用了工厂方法模式。

> 工厂方法模式：定义一个用于创建对象的接口，让子类决定实例化哪一个类。Factory Method 使一个类的实例化延迟到其子类。

我们将 BeanIocContainer 修改为 BeanFactory 接口，即提供 getBean() 方法。创建容器由其子类自己实现。

ApplicationContext 和 BeanFactory 的区别：ApplicationContext 初始化时就实例化所有 Bean，BeanFactory 用到时再实例化所用 Bean

源码对应：**v3**

## 三、实现注解

前面使用 xml 配置文件的方式，实现了实例化 bean 和依赖注入。这种方式比较麻烦，还容易出错。Spring 从 2.5 开始可使用注解替代 xml 配置文件<sup>[ref](https://www.tutorialspoint.com/spring/spring_annotation_based_configuration.htm)</sup>。实现如下：

1. 使用 @Component 代替 `<bean>`，使用 @Autowired 代替 `<property>`
2. 利用 `<context:component-scan>` 指定扫描路径

原理：

1、@Component 用于生成 BeanDefinition

- 根据 component-scan 指定路径，找到路径下所有包含 @Component 注解的 Class 文件，作为 BeanDefinition
- 如何判断 Class 是否有 @Componet：使用 ClassReader 利用字节码技术，获取 Class 文件中的元数据（注解等），判断元数据中是否有 @Componet。注解是 Class 文件中的元数据。

2、@Autowired 用于依赖注入

- 通过反射，查看 Field 中是否有 @Autowired 类型的注解，有，则使用反射实现依赖注入`field.set(bean, value)`

此时，我们还是在使用配置文件，我们可以使用 @Configuration 替代配置文件，并使用 @ComponentScan 来替代配置文件的  `<context:component-scan>` 。

```Java
@Configuration
@ComponentScan // ComponentScan 扫描 PetStoreConfig 所在路径及其所在路径所有子路径
public class PetStoreConfig {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(PetStoreConfig.class);
        PetStoreService userService = context.getBean(PetStoreService.class);
        userService.getAccountDao();
    }
}
```

使用 @Configuration 和使用 @ComponentScan，其实跟使用配置文件一样，只是将 xml 形式替代为注解的形式。同样是根据 @ComponentScan 的路径，遍历找到 class，判断是否有 @Component 注解 ...

AnnotationConfigApplicationContext 是专为针对以类作为启动配置的启动类。关于AnnotationConfigApplicationContext 的实现机制，可以网上查看。

名词解释：

- Component：组件
- Autowired：自动装配

源码对应：**v4**

## 四、Spring Boot 原理

说到了 @Configuration 和 @ComponentScan，就不得不提 Spring Boot，因为 Spring Boot 就使用了  @Configuration 和 @ComponentScan，你可以点开 @SpringBootApplication 看到。

我们发现，Spring Boot 启动时，并没有指定使用 `**`ApplicationContext + `**`Config。这是因为他使用了 @EnableAutoConfiguration 注解。

Spring Boot 利用了 @EnableAutoConfiguration 来自动加载 spring.factories 中的配置类到容器。Spring Boot 默认加载的配置类：

![img](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-05-092624.png?ynotemdtimestamp=1587214457308)

这也是 Spring Boot 的核心知识点。

**starter 包**

在 Spring Boot 中，我们引入的 jar 包都有一个字段，**starter**，我们叫 starter 包。

![image-20200418213313297](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-18-133313.png)

标识为 starter（启动器）是因为引入这些包时，我们不用设置额外的去设置配置文件，starter 包均包含自己的 spring.factories，启动类会自动将配置文件加载进容器。

![image-20200418214517465](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-18-134518.png)

自定义 starter 包**

@EnableAutoConfiguration + spring.factories 的其他使用栗子：

1. 我们常引用的 starter 包，如：druid-spring-boot-starter，自动
2. 也都有 spring.factories
3. 自定义 starter 包

1、在 Spring Cloud 中，当某个应用要调用另一个应用的模块时，要么调用方使用 Feign（HTTP），要么被调用方作为依赖被调用方引入，使用  @Autowired。本质上是将被调用方的 Bean 注入容器，所以被调用方需要如下设置：

```Java
@Configuration
@ComponentScan
public class ProviderAppConfiguration {
}

// spring.factories
# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.amy.cloud.amycloudact.ProviderAppConfiguration
```

![image-20200418191026166](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-18-111026.png)

2、我们常引用的 starter 包，如：druid-spring-boot-starter，也都有 spring.factories

<!--3、dubbo 的作用：像调用本地容器接口一样调用远程接口-->

说了 Spring Boot，在 Spring MVC，引入 jar 的包将配置类如何注入容器？

- 通过配置文件：

```xml
<import resource="classpath*:shiro.xml"/>
```

jdk jar 包、工具 jar 包的类是否需要注入容器？

- 容器只是为了管理业务类，注入容器的类都有 @Component 注解。

多个请求并发访问时，是否有多个容器？

- 容器是对象属性，线程共享，多个线程可同时操作，为了保证线程安全，用 final 修饰。

## 结语

以上主要实现了 Spring 框架的 Bean 创建功能，Spring 框架还有很多重要功能，如：Bean 生命周期管理（回收）、AOP 的实现，等等。后续有机会再做一次分享。

虽然现在大部分都是 SpringBoot，大家可能对 xml + SpringMVC 嗤之以鼻。但知其然，才能知其所以然，虽然现在不用，但还是有了解它的必要性。

你可能会有这样的疑问：

jdk jar 包、工具 jar 包的类是否需要注入容器？

- 容器只是为了管理业务类，注入容器的类都有 @Component 注解。
