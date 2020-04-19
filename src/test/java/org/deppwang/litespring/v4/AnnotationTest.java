package org.deppwang.litespring.v4;


import org.deppwang.litespring.v4.context.ClassPathXmlApplicationContext;
import org.deppwang.litespring.v4.dao.AccountDao;
import org.deppwang.litespring.v4.service.PetStoreService;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AnnotationTest {
    @Test
    public void test() {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("petstore-v4.xml");
        PetStoreService petStore = (PetStoreService) ctx.getBean("petStore");

        assertNotNull(petStore.getAccountDao());
        System.out.println(petStore.getName());

        assertTrue(petStore.getAccountDao() instanceof AccountDao);
    }
}
