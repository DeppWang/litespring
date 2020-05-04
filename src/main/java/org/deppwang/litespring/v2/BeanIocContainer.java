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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanIocContainer {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

    public BeanIocContainer(String configFile) {
        loadBeanDefinitions(configFile);
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
                parseConstructorArgElement(ele, bd);
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
     * 获取构造方法的参数名
     *
     * @param beanElem
     * @param bd
     */
    private void parseConstructorArgElement(Element beanElem, BeanDefinition bd) {
        Iterator iter = beanElem.elementIterator("constructor-arg");
        while (iter.hasNext()) {
            Element propElem = (Element) iter.next();
            String argumentName = propElem.attributeValue("ref");
            if (!StringUtils.hasLength(argumentName)) {
                return;
            }

            bd.getConstructorArgumentValues().add(argumentName);
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
        if (bd.hasConstructorArgumentValues()) {
            return autowireConstructor(bd);
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            String beanClassName = bd.getBeanClassName();
            try {
                // 通过类加载器，根据 className（class 路径），得到其类对象
                Class<?> clz = cl.loadClass(beanClassName);
                // 通过类对象利用反射创建 Bean 实例
                return clz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 通过构造方法实现依赖注入
     *
     * @param bd
     * @return
     */
    private Object autowireConstructor(final BeanDefinition bd) {
        Constructor<?> constructorToUse = null; // 代表最终匹配的 Constructor 对象
        Object[] argsToUse = null; // 代表将依赖注入的对象
        try {
            Class<?> beanClass = Thread.currentThread().getContextClassLoader().loadClass(bd.getBeanClassName());
            // 通过反射获取当前类的所有 Constructor 对象（包括私有和公有）
            Constructor<?>[] candidates = beanClass.getDeclaredConstructors();
            for (int i = 0; i < candidates.length; i++) {

                Class<?>[] parameterTypes = candidates[i].getParameterTypes();
                if (parameterTypes.length != bd.getConstructorArgumentValues().size()) {
                    continue;
                }
                // 设置构造方法参数实例
                argsToUse = new Object[parameterTypes.length];
                valuesMatchTypes(bd.getConstructorArgumentValues(), argsToUse);
                constructorToUse = candidates[i];
                break;
            }
            // 使用带有参数的构造方法对象实现实例化 Bean
            return constructorToUse.newInstance(argsToUse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置构造方法参数实例
     *
     * @param beanNames
     * @param argsToUse
     */
    private void valuesMatchTypes(
            List<String> beanNames,
            Object[] argsToUse) {
        for (int i = 0; i < beanNames.size(); i++) {
            Object argumentBean = getBean(beanNames.get(i));
            argsToUse[i] = argumentBean;
        }
    }

    /**
     * 填充属性（依赖注入）
     * 有 setter 方法，利用 Method 的 Method.invoke()；没有 setter 方法（使用注解时），利用 Field 的 field.set()
     *
     * @param bd
     * @param bean
     */
    private void populateBean(BeanDefinition bd, Object bean) {
        List<String> propertyNames = bd.getPropertyNames();
        try {
//            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
//            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
//            for (String propertyName : propertyNames) {
//                Object propertyBean = getBean(propertyName);
//                for (PropertyDescriptor pd : pds) {
//                    if (pd.getName().equals(propertyName)) {
//                        // 利用反射（Method.invoke()），通过 setter 方法将 bean 的字段关联到对象实例
//                        pd.getWriteMethod().invoke(bean, propertyBean);
//                        break;
//                    }
//                }
//            }
            // 通过反射获取当前类所有的方法信息（Method 对象）
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (String propertyName : propertyNames) {
                for (Method method : methods) {
                    if (method.getName().equals("set" + upperCaseFirstChar(propertyName))) {
                        // 获得方法参数实例
                        Object propertyBean = getBean(propertyName);
                        // 通过反射执行调用 setter() 方法。invoke：调用方法，propertyBean 作为方法的参数
                        method.invoke(bean, propertyBean);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    private String upperCaseFirstChar(String str) {
        char chars[] = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
}
