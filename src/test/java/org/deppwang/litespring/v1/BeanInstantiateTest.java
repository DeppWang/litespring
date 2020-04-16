package org.deppwang.litespring.v1;


import org.deppwang.litespring.v1.service.PetStoreService;
import org.junit.Assert;
import org.junit.Test;

public class BeanInstantiateTest {

    @Test
    public void test() {
        BeanIocContainer iocContainer = new BeanIocContainer("petstore-v1.xml");

        BeanDefinition bd = iocContainer.getBeanDefinition("petStore");

        Assert.assertEquals("org.deppwang.litespring.v1.PetStoreService", bd.getBeanClassName());

        PetStoreService petStore = (PetStoreService) iocContainer.getBean("petStore");

        Assert.assertNotNull(petStore);
    }
}
