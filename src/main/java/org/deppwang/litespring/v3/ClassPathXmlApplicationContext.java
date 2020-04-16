package org.deppwang.litespring.v3;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoC 容器，本质上是一个 ConcurrentHashMap，key 为 beanId，value 为 Bean
 */
public class ClassPathXmlApplicationContext implements BeanFactory {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

    // 使用 ConcurrentHashMap 存放所有单例 Bean，String 为 beanId（beanName）
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(64);

    /**
     * 跟 XmlBeanFactory 的区别在于提前将所用 Bean 注入容器
     *
     * @param configFile
     */
    public ClassPathXmlApplicationContext(String configFile) {
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
            // 遍历所有 bean
            while (iter.hasNext()) {
                Element ele = (Element) iter.next();
                String id = ele.attributeValue("id");
                String beanClassName = ele.attributeValue("class");
                BeanDefinition bd = new BeanDefinition(id, beanClassName);
                parsePropertyElement(ele, bd);
                this.beanDefinitionMap.put(id, bd);
            }
        } catch (DocumentException e) {
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


    private void parsePropertyElement(Element beanElem, BeanDefinition bd) {
        Iterator iter = beanElem.elementIterator("property");
        while (iter.hasNext()) {
            Element propElem = (Element) iter.next();
            String propertyName = propElem.attributeValue("name");
            if (!StringUtils.hasLength(propertyName)) {
                return;
            }

            bd.getPropertyNames().add(propertyName);
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
     * 根据 beanId 获取实例，没有则生成
     *
     * @param beanId
     * @return
     */
    public Object getBean(String beanId) {
        BeanDefinition bd = this.getBeanDefinition(beanId);
        // 单例模式，一个类对应一个 Bean，不是通过 id。常规单例模式是多次调用方法，只生成一个实例。此处是只会调用依次生成实例方法。
        Object bean = this.getSingleton(beanId);
        if (bean == null) {
            bean = createBean(bd);
            this.registerSingleton(beanId, bean);
        }
        return bean;
    }


    public BeanDefinition getBeanDefinition(String beanID) {
        return this.beanDefinitionMap.get(beanID);
    }

    /**
     * 将单例 Bean 存放到 singletonObjects 中
     * @param beanName
     * @param singletonObject
     */
    public void registerSingleton(String beanName, Object singletonObject) {
        Object oldObject = this.singletonObjects.get(beanName);
        if (oldObject != null) {
            System.out.println("error," + oldObject + "had already registered");
        }
        this.singletonObjects.put(beanName, singletonObject);
    }

    /**
     * 根据 beanName，从 singletonObjects 中获取到实例
     * @param beanName
     * @return
     */
    public Object getSingleton(String beanName) {

        return this.singletonObjects.get(beanName);
    }

    private Object createBean(BeanDefinition bd) {
        // 创建实例
        Object bean = instantiateBean(bd);
        // 填充属性（依赖注入）
        populateBean(bd, bean);

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

    void populateBean(BeanDefinition bd, Object bean) {
        List<String> pns = bd.getPropertyNames();
        // 通过 Method.invoke() 设置属性到 bean 中
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (String pn : pns) {
                Object propertyBean = getBean(pn);
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equals(pn)) {
                        // 通过 setter 方法为 bean 设置属性
                        pd.getWriteMethod().invoke(bean, propertyBean);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
