package org.deppwang.litespring.v4;

import org.deppwang.litespring.v4.entry.Account;
import org.deppwang.litespring.v4.entry.Item;
import org.deppwang.litespring.v4.service.PetStoreService;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AnnotationTest {
    @Test
    public void test() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("petstore-v4.xml");

        PetStoreService petStore = (PetStoreService) ctx.getBean("petStoreService");

        assertNotNull(petStore.getAccount());
        assertNotNull(petStore.getItem());

        assertTrue(petStore.getAccount() instanceof Account);
        assertTrue(petStore.getItem() instanceof Item);
    }
}
