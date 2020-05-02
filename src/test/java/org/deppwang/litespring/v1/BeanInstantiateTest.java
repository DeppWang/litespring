package org.deppwang.litespring.v1;


import org.deppwang.litespring.v1.dao.AccountDao;
import org.deppwang.litespring.v1.service.PetStoreService;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class BeanInstantiateTest {

    @Test
    public void test() {
        BeanIocContainer iocContainer = new BeanIocContainer("petstore-v1.xml");

        BeanDefinition bd = iocContainer.getBeanDefinition("petStore");

        Assert.assertEquals("org.deppwang.litespring.v1.service.PetStoreService", bd.getBeanClassName());

        PetStoreService petStore = (PetStoreService) iocContainer.getBean("petStore");

        Assert.assertNotNull(petStore);

        AccountDao accountDao = (AccountDao) iocContainer.getBean("accountDao");
        Assert.assertNotNull(accountDao);
    }

    @Test
    public void testFeild() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            // 通过类加载器，根据 className（全限定名），得到其类对象，通过类对象利用反射创建 Bean 实例
//            Class<?> clz = cl.loadClass("org.deppwang.litespring.v1.service.PetStoreService");
            Class clz = PetStoreService.class;
//            Field[] fields = clz.getDeclaredFields();
            PetStoreService storeService = (PetStoreService) clz.newInstance();
//            storeService.getClass();
            Field field = clz.getDeclaredField("name");
//            Field[] arr = clz.getDeclaredFields();
            System.out.println(field.get(storeService));
            System.out.println(clz.getDeclaredField("i").get(storeService));
//            Unsafe.class.putObject(storeService, 16, "test3");
            field.set(storeService, "test2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(expected = SecurityException.class)
    public void testSingletonGetter() throws Exception {
        Unsafe unsafe = Unsafe.getUnsafe();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> clz = cl.loadClass("org.deppwang.litespring.v1.service.PetStoreService");
        Class s = PetStoreService.class;
        PetStoreService storeService = (PetStoreService) clz.newInstance();
        long l = 16;
        unsafe.putObject(storeService, l, "test3");
    }

    @Test(expected = SecurityException.class)
    public void testUnsafe() throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> clz = cl.loadClass("org.deppwang.litespring.v1.service.PetStoreService");
        PetStoreService storeService = (PetStoreService) clz.newInstance();
        long l = 16;
        unsafe.putObject(storeService, l, "test3");
    }

    @Test(expected = SecurityException.class)
    public void testUnsafe2() throws Exception {
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        Unsafe unsafe = unsafeConstructor.newInstance();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> clz = cl.loadClass("org.deppwang.litespring.v1.service.PetStoreService");
        PetStoreService storeService = (PetStoreService) clz.newInstance();
        long l = 16;
        unsafe.putObject(storeService, l, "test3");
    }

    @Test
    public void testMethod() {
//        Method[] methods = bean.getClass().getMethods();
        try {
            Class cls = Class.forName("org.deppwang.litespring.v2.service.PetStoreService");
//            cls.getDeclaredMethods0(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
