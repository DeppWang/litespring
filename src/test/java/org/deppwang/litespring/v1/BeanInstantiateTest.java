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
}
