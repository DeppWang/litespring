package org.deppwang.litespring.v2;

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
 * IoC 容器，本质上是一个 ConcurrentHashMap，key 为 beanId，value 为 BeanDefinition
 */
public class BeanIocContainer {

    // 使用 Map 存放所有 BeanDefinition，ConcurrentHashMap 保证线程安全
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

    /**
     * 设置 bean 的所有属性名
     *
     * @param beanElem
     * @param bd
     */
    public void parsePropertyElement(Element beanElem, BeanDefinition bd) {
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
     * 通过 beanID，返回对应 BeanDefinition
     *
     * @param beanID
     * @return
     */
    public BeanDefinition getBeanDefinition(String beanID) {
        return this.beanDefinitionMap.get(beanID);
    }

    /**
     * 根据 beanID 获取（生成）实例
     *
     * @param beanID
     * @return
     */
    public Object getBean(String beanID) {
        BeanDefinition bd = this.getBeanDefinition(beanID);
        return createBean(bd);
    }

    /**
     * 根据 BeanDefinition 生成实例
     *
     * @param bd
     * @return
     */
    private Object createBean(BeanDefinition bd) {
        // 创建实例
        Object bean = instantiateBean(bd);
        // 填充属性（依赖注入）
        populateBean(bd, bean);

        return bean;
    }

    /**
     * 创建实例
     *
     * @param bd
     * @return
     */
    private Object instantiateBean(BeanDefinition bd) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String beanClassName = bd.getBeanClassName();
        try {
            // 通过类加载器，根据 className（全限定名），得到其类对象，通过类对象利用反射创建 Bean 实例
            Class<?> clz = cl.loadClass(beanClassName);
            return clz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 填充属性（依赖注入）
     * 有 setter 方法，利用 PropertyDescriptor 的 Method.invoke()；没有 setter 方法，利用 Field 的 field.set()
     *
     * @param bd
     * @param bean
     */
    private void populateBean(BeanDefinition bd, Object bean) {
        List<String> propertyNames = bd.getPropertyNames();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (String propertyName : propertyNames) {
                Object propertyBean = getBean(propertyName);
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equals(propertyName)) {
                        // 利用反射（Method.invoke()），通过 setter 方法将 bean 的属性关联到对象实例
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
