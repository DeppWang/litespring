package org.deppwang.litespring.v1;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此时 Spring 本质上是一个 ConcurrentHashMap，key 为 beanId，value 为 BeanDefinition
 */
public class BeanIocContainer {

    // 使用 Map 存放所有 BeanDefinition，String 为 beanID。ConcurrentHashMap 保证线程安全
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

    public BeanIocContainer(String configFile) {
        loadBeanDefinitions(configFile);
    }

    /**
     * 找到 xml 中所有 bean 相关信息，转换为 beanDefinition，存放到 beanDefinitionMap 中
     * 需要借助 dom4j
     *
     * @param configFile
     */
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

    /**
     * 通过 beanID，返回对应 BeanDefinition
     *
     * @param beanID
     * @return
     */
    public BeanDefinition getBeanDefinition(String beanID) {
        return this.beanDefinitionMap.get(beanID);
    }

    /**
     * 通过 beanID 得到 BeanDefinition 中对应的全限定名；
     * 通过类加载器，根据 className（全限定名），得到其类对象；
     * 通过类对象利用反射创建 Bean 实例
     *
     * @param beanID
     * @return
     */
    public Object getBean(String beanID) {
        BeanDefinition bd = this.getBeanDefinition(beanID);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String beanClassName = bd.getBeanClassName();
        try {
            // 通过类加载器，根据 className（全限定名），得到其类对象
            Class<?> clz = cl.loadClass(beanClassName);
            // 通过类对象利用反射创建 Bean 实例
            return clz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
