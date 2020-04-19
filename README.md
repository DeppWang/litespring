# 造轮子：实现一个简易的 Spring IoC 容器

![source:https://fernandofranzini.wordpress.com/](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-19-110638.jpg)

我通过实现一个简易的 Spring IoC 容器，算是入门了 Spring 框架。本文是对实现过程的一个总结提炼，**需要配合源码阅读**，[源码地址](https://github.com/DeppWang/litespring)。

结合本文和源码，你应该可以学到：Spring 的原理和 Spring Boot 的原理。

Spring 框架是 Java 开发的，Java 是面向对象的语言，所以 Spring 框架本身有大量的抽象、继承、多态。对于初学者来说，光是理清他们的逻辑就很麻烦，我摒弃了那些包装，只实现了最本质的功能。代码不是很严谨，但只为了理解 Spring 思想却够了。

下面正文开始。

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

到处都是  **new** 关键字，需要开发人员显式的使用 new 关键字来创建**业务类**对象（实例）。这样有很多弊端，如，创建的对象太多，耦合性太强，等等。

有个叫 [Rod Johnson](https://en.wikipedia.org/wiki/Rod_Johnson_(programmer)) 老哥对此很不爽，就开发了一个叫 [Spring](https://spring.io/) 的框架，就是为了干掉 new 关键字（哈哈，我杜撰的，只是为了说明 Spring 的作用）。

有了 Spring 框架，**由框架来新建对象，管理对象，并处理对象之间的依赖**，我们程序员再也不用 new 业务类对象了。我们来看看 Spring 框架是如何实现的吧。

注：以下 Spring 框架简写为 Spring

本节源码对应：**v0**

## 一、实现「实例化 Bean 」

首先，Spring 需要实例化类，将其转换为对象。在 Spring 中，我们管（业务）类叫 Bean，所以实例化类也可以称为实例化 Bean。

早期 Spring 需要借助 xml 配置文件来实现实例化 Bean，可以分为三步（配合源码 **v1** 阅读）：

1. 从 xml 配置文件获取 Bean 信息，如全限定名等，将其作为 BeanDefinition（Bean 定义类）的属性
2. 使用一个 Map 存放所有 BeanDefinition，此时 Spring 本质上是一个 Map，存放 BeanDefinition
3. 当获取 Bean 实例时，通过类加载器，根据全限定名，得到其类对象，通过类对象利用**反射**创建 Bean 实例

关于类加载和反射，前者可以看看[《深入理解 Java 虚拟机》](https://book.douban.com/subject/34907497/)第 7 章，后者可以看看《廖雪峰 Java 教程》[反射](https://www.liaoxuefeng.com/wiki/1252599548343744/1255945147512512) 部分。本文只学习 Spring，这两个知识点不做深入讨论。

名词解释：

- 全限定名：指编译后的 class 文件在 jar 包中的路径

本节源码对应：**v1**

## 二、实现「填充属性（依赖注入）」

实现实例化 Bean 后，此时成员变量（引用）还为 null：

![](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-17-143303.png)

此时需要通过一种方式实现，让引用指向实例，我们管这一步叫填充属性。

当一个 Bean 的成员变量类型是另一个 Bean 时，我们可以说一个 Bean 依赖于另一个 Bean。所以填充属性，也可以称为依赖注入（**D**ependency **I**njection，简称 DI）。

抛开 Spring 不谈，在正常情况下，我们有两种方式实现依赖注入，1、使用 Setter() 方法，2、使用构造方法。使用 Setter() 方法如下：

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

其实早期 Spring 也是通过这两种方式来实现依赖注入的。下面是 Spring 通过 xml 文件 + Setter() 来实现依赖注入的步骤（配合源码 **v2** 阅读）：

1. 给 PetStoreService 添加 Setter() 方法，并稍微修改一下 xml 配置文件，添加 `<property>`，代表对应 Setter() 方法。
2. 从 xml 配置文件获取 Bean 的属性 `<property>`，存放到 BeanDefinition 的 propertyNames 中。
3. 通过 propertyName 获取属性实例，利用反射，通过 Setter() 方法实现填充属性（依赖注入）

基于构造函数实现依赖注入的方式跟 Setter() 方法差不多，感兴趣可以 Google 搜索查看。

因为 Spring 实现了依赖注入，所以我们程序员没有了创建对象的控制权，所以也被称为控制反转（**I**nversion **o**f **C**ontrol，简称  IoC）。因为 Spring 使用 Map 管理 BeanDefinition，我们也可以将 Spring 称为 IoC 容器。

本节源码对应：**v2**

## 三、使用「单例模式、工厂方法模式」

前面两步实现了获取 Bean 实例时创建 Bean 实例，但 Bean 实例经常使用，不能每次都新创建。其实在 Spring 中，一个业务类只对应一个 Bean 实例，这需要使用单例模式。

> 单例模式：一个类有且只有一个实例

Spring 使用类对象创建 Bean 实例，是如何实现单例模式的？

<!--正常单例模式通过类或者枚举来生成对象，Spring 使用类对象创建 Bean 实例，两者不太一样。-->

Spring 其实使用一个 Map 存放所有 Bean 实例。创建时，先看 Map 中是否有 Bean 实例，没有就创建；获取时，直接从 Map 中获取。这种方式能保证一个类只有一个 Bean 实例。

```Java
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(64);
```

早期 Spring 使用 Bean 的策略是用到时再实例化所用 Bean，杰出代表是 XmlBeanFactory，后期为了实现更多的功能，新增了 ApplicationContext，两者都继承于 BeanFactory 接口。这使用了工厂方法模式。

> 工厂方法模式：定义一个用于创建对象的接口，让子类决定实例化哪一个类。Factory Method 使一个类的实例化延迟到其子类。

我们将 BeanIocContainer 修改为 BeanFactory 接口，只提供 getBean() 方法。创建（IoC）容器由其子类自己实现。

ApplicationContext 和 BeanFactory 的区别：ApplicationContext 初始化时就实例化所有 Bean，BeanFactory 用到时再实例化所用 Bean。

本节源码对应：**v3**

## 三、实现「注解」

前面使用 xml 配置文件的方式，实现了实例化 Bean 和依赖注入。这种方式比较麻烦，还容易出错。Spring 从 2.5<sup>[ref](https://www.tutorialspoint.com/spring/spring_annotation_based_configuration.htm)</sup> 开始可使用注解替代 xml 配置文件。比如：

1. 使用 @Component 注解代替 `<bean>`
2. 使用 @Autowired 注解代替 `<property>`

@Component 用于生成 BeanDefinition，原理（配合源码 **v4** 阅读）：

- 根据 component-scan 指定路径，找到路径下所有包含 @Component 注解的 Class 文件，作为 BeanDefinition
- 如何判断 Class 是否有 @Component：利用字节码技术，获取 Class 文件中的元数据（注解等），判断元数据中是否有 @Componet

@Autowired 用于依赖注入，原理（配合源码 **v4** 阅读）：

- 通过反射，查看 Field 中是否有 @Autowired 类型的注解，有，则使用反射实现依赖注入

至此，我们还是在需要通过配置文件来实现组件扫描。有没有完全不使用配置文件的方式？有！

我们可以使用 @Configuration 替代配置文件，并使用 @ComponentScan 来替代配置文件的  `<context:component-scan>` 。

```Java
@Configuration // 将类标记为 @Configuration，代表这个类是相当于一个配置文件
@ComponentScan // ComponentScan 扫描 PetStoreConfig.class 所在路径及其所在路径所有子路径的文件
public class PetStoreConfig {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(PetStoreConfig.class);
        PetStoreService userService = context.getBean(PetStoreService.class);
        userService.getAccountDao();
    }
}
```

使用注解其实跟使用 xml 配置文件一样，目的是将配置类作为入口，实现扫描组件，将其加载进 IoC 容器中的功能。

AnnotationConfigApplicationContext 是专为针对配置类的启动类。其实现机制，可以 Google 查阅。

名词解释：

- Component：组件
- Autowired：自动装配

本节源码对应：**v4**

## 四、Spring Boot 原理

说到了 @Configuration 和 @ComponentScan，就不得不提 Spring Boot，因为 Spring Boot 就使用了  @Configuration 和 @ComponentScan，你可以点开 @SpringBootApplication 看到。

我们发现，Spring Boot 启动时，并没有使用 AnnotationConfigApplicationContext 来指定启动某某 Config 类。这是因为它使用了 @EnableAutoConfiguration 注解。

Spring Boot 利用了 @EnableAutoConfiguration 来自动加载标识为 @Configuration 的配置类到容器中。Spring Boot 还可以将需要自动加载的配置类放在 spring.factories 中，Spring Boot 将自动加载 spring.factories 中的配置类。spring.factories 需放置于META-INF 下。

如 Spring Boot 项目启动时，autocofigure 包中将自动加载到容器的（部分）配置类如下：

![](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-05-092624.png?ynotemdtimestamp=1587214457308)

以上也是 Spring Boot 的原理。

在 Spring Boot 中，我们引入的 jar 包都有一个字段，**starter**，我们叫 starter 包。

![](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-18-151532.png)

标识为 starter（启动器）是因为引入这些包时，我们不用设置额外操作，它能被自动装配，starter 包一般都包含自己的 spring.factories。如 spring-cloud-starter-eureka-server：

![](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-18-134518.png)

如 druid-spring-boot-starter：

![](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-19-085235.png)

有时候我们还需要自定义 starter 包，比如在 Spring Cloud 中，当某个应用要调用另一个应用的代码时，要么调用方使用 Feign（HTTP），要么将被调用方自定义为 starter 包，让调用方依赖引用，再 @Autowired 使用。此时需要在被调用方设置配置类和 spring.factories：

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

![](https://deppwang.oss-cn-beijing.aliyuncs.com/blog/2020-04-18-111026.png)

当然，你也可以把这两个文件放在调用方（此时要指定扫描路径），但一般放在被调用方。ps：如果你两个应用的 base-package 路径一样，那么可以不用这一步。

说了 Spring Boot，那么在 Spring MVC，如何将引入 jar 包的组件注入容器？

- 跟扫描本项目包一样，在 xml ，增加引入 jar 包的扫描路径：

```xml
<context:component-scan base-package="引入 jar 包的 base-package" />
...
```

## 五、结语

以上主要实现了 Spring 如何作为一个 IoC 容器，顺便说了一下 Spring Boot 原理。当然，Spring 还有很多重要功能，如：Bean 生命周期管理（回收）、AOP 的实现，等等。后续有机会再做一次分享。

在 Spring Boot 主

来个注解小结：

- Spring 只实例化标识为 @Component 的组件（即业务类对象）
- @Component 作为组件标识
- @Autowired 用于判断是否需要依赖注入
- @ComponentScan 指定组件扫描路径，不指定即为当前路径
- @Configuration 代表配置类，作为入口
- @EnableAutoConfiguration 实现加载配置类

有的童鞋可能还会有这样的疑问：

> jdk jar 包、工具 jar 包的类是否需要注入容器？

- 回答是不需要，因为容器只管理业务类，注入容器的类都有 @Component 注解。

全文完。