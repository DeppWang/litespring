package org.deppwang.litespring.v4.context;

import org.deppwang.litespring.v4.BeanDefinition;
import org.deppwang.litespring.v4.BeanFactory;
import org.deppwang.litespring.v4.annotation.AnnotationMetadata;
import org.deppwang.litespring.v4.stereotype.Autowired;
import org.deppwang.litespring.v4.stereotype.Component;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.asm.ClassReader;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ClassPathXmlApplicationContext implements BeanFactory {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(64);

    // 存放注解
    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes =
            new LinkedHashSet<>();

    public ClassPathXmlApplicationContext(String configFile) {
        this.autowiredAnnotationTypes.add(Autowired.class);
        loadBeanDefinitions(configFile);
        prepareBeanRegister();
    }

    private void loadBeanDefinitions(String configFile) {
        InputStream is = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            is = cl.getResourceAsStream(configFile); // 根据 configFile 获取 petstore-v1.xml 文件的字节流

            SAXReader reader = new SAXReader();
            Document doc = reader.read(is); // 将字节流转成文档格式

            Element root = doc.getRootElement(); // <beans>
            Iterator iter = root.elementIterator();
            while (iter.hasNext()) {
                Element ele = (Element) iter.next();
                parseComponentElement(ele);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 根据 component-scan 指定路径，找到路径下所有包含 @Component 注解的 Class 文件，作为 beanDefinition
     *
     * @param ele
     */
    private void parseComponentElement(Element ele) throws IOException {

        // 获取 component-scan 的路径
        String basePackagesStr = ele.attributeValue("base-package");
        String[] basePackages = basePackagesStr.split(",");
        for (String basePackage : basePackages) {

            File[] files = getFiles(basePackage);
            for (File file : files) {
                AnnotationMetadata annotationMetadata = getAnnotationMetadata(file);
                // 通过 hasAnnotation 判断是否有 @Component 注解
                if (annotationMetadata.hasAnnotation(Component.class.getName())) {
                    String beanId = (String) annotationMetadata.getAnnotationAttributes(Component.class.getName()).get("value");

                    String beanClassName = annotationMetadata.getClassName();
                    if (beanId == null) {
                        // 通过全限定名获取类名，并将首字母小写
                        beanId = Introspector.decapitalize(ClassUtils.getShortName(beanClassName));
                    }

                    BeanDefinition bd = new BeanDefinition(beanId, beanClassName);
                    this.beanDefinitionMap.put(beanId, bd);
                }
            }
        }
    }

    /**
     * ApplicationContext 特点，第一次加载即注入所有 bean 到容器
     */
    private void prepareBeanRegister() {
        for (String beanId : beanDefinitionMap.keySet()) {
            BeanDefinition bd = this.getBeanDefinition(beanId);
            // 单例模式，一个类对应一个 Bean，不是通过 id。常规单例模式是多次调用方法，只生成一个实例。此处是只会调用依次生成实例方法。
            Object bean = this.getSingleton(beanId);
            if (bean == null) {
                bean = createBean(bd);
                this.registerSingleton(beanId, bean);
            }
        }
    }

    /**
     * 利用字节码技术，将注解元数据存放在 AnnotationMetadata 中，一个 file 对应一个 AnnotationMetadata
     *
     * 待优化：去除 AnnotationMetadata，直接获取注解
     *
     * @param file
     * @return
     * @throws IOException
     */
    public AnnotationMetadata getAnnotationMetadata(File file) throws IOException {

        // file 是路径，is 相当于字节码文件流
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        // 此时使用了 Spring 框架的 ClassReader，待优化为使用原生类
        ClassReader classReader;

        try {
            // 通过文件流设置 classReader
            classReader = new ClassReader(is);
        } finally {
            is.close();
        }

        AnnotationMetadata visitor = new AnnotationMetadata();
        // classReader 利用字节码技术，从文件流中获取元数据，设置到 AnnotationMetadata 中
        classReader.accept(visitor, ClassReader.SKIP_DEBUG);

        return visitor;
    }

    /**
     * 获取指定路径下的所有 Class 文件
     *
     * @param basePackage
     * @return
     */
    private File[] getFiles(String basePackage) {
        String location = ClassUtils.convertClassNameToResourcePath(basePackage);
        URL url = Thread.currentThread().getContextClassLoader().getResource(location);
        File rootDir = new File(url.getFile());
        Set<File> matchingFiles = new LinkedHashSet<>(8);
        doRetrieveMatchingFiles(rootDir, matchingFiles);
        return matchingFiles.toArray(new File[0]);
    }

    /**
     * 通过递归获取文件夹下的文件
     *
     * @param dir
     * @param result
     */
    private void doRetrieveMatchingFiles(File dir, Set<File> result) {

        File[] dirContents = dir.listFiles();
        if (dirContents == null) {
            return;
        }
        for (File content : dirContents) {
            if (content.isDirectory()) {
                if (!content.canRead()) {
                } else {
                    doRetrieveMatchingFiles(content, result);
                }
            } else {
                result.add(content);
            }
        }
    }


    public Object getBean(String beanID) {
        BeanDefinition bd = this.getBeanDefinition(beanID);
        // 单例模式，一个类对应一个 Bean，不是通过 id。常规单例模式是多次调用方法，只生成一个实例。此处是只会调用依次生成实例方法。
        Object bean = this.getSingleton(beanID);
        if (bean == null) {
            bean = createBean(bd);
            this.registerSingleton(beanID, bean);
        }
        return bean;
    }

    public BeanDefinition getBeanDefinition(String beanID) {

        return this.beanDefinitionMap.get(beanID);
    }


    private void registerSingleton(String beanName, Object singletonObject) {
        Object oldObject = this.singletonObjects.get(beanName);
        if (oldObject != null) {
            System.out.println("error," + oldObject + " had already registered");
        }
        this.singletonObjects.put(beanName, singletonObject);
    }

    private Object getSingleton(String beanName) {
        return this.singletonObjects.get(beanName);
    }


    private Object createBean(BeanDefinition bd) {
        // 创建实例
        Object bean = instantiateBean(bd);
        // 填充属性（依赖注入）
        populateBean(bean);

        return bean;
    }


    private Object instantiateBean(BeanDefinition bd) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String beanClassName = bd.getBeanClassName();
        try {
            // 根据 className，利用反射创建 Bean 实例
            Class<?> clz = cl.loadClass(beanClassName);
            return clz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 利用反射，将属性与对象关联
     * 没有 setter 方法，利用 Field 的field.set()；有 setter 方法，利用 PropertyDescriptor 的 Method.invoke()
     *
     * @param bean
     */
    private void populateBean(Object bean) {
        // 利用反射，获取所有的属性，包括私有属性
        Field[] fields = bean.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                Annotation ann = findAutowiredAnnotation(field);
                // 根据是否有 Autowired 注解来决定是否注入
                if (ann != null) {
                    Object value = getBean(field.getName());
                    if (value != null) {
                        // 设置私有属性可连接
                        makeAccessible(field);
                        // 利用反射，将属性的实例和属性相关联
                        field.set(bean, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查看 field 是否有注解。
     *
     * @param ao
     * @return
     */
    private Annotation findAutowiredAnnotation(AccessibleObject ao) {
        for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
            // type: org.deppwang.litespring.v4.stereotype.Autowired
            Annotation ann = getAnnotation(ao, type);
            if (ann != null) {
                return ann;
            }
        }
        return null;
    }

    /**
     * 通过反射，查看 field 是否有 annotationType 类型的注解
     *
     * @param ae
     * @param annotationType
     * @param <T>
     * @return
     */
    private <T extends Annotation> T getAnnotation(AnnotatedElement ae, Class<T> annotationType) {
        T ann = ae.getAnnotation(annotationType);
        if (ann == null) {
            for (Annotation metaAnn : ae.getAnnotations()) {
                ann = metaAnn.annotationType().getAnnotation(annotationType);
                if (ann != null) {
                    break;
                }
            }
        }
        return ann;
    }

    /**
     * 设置私有属性可连接
     *
     * @param field
     */
    private void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }
}
