package org.deppwang.litespring.v2;

import org.deppwang.litespring.v2.entry.Account;
import org.deppwang.litespring.v2.entry.Item;
import org.deppwang.litespring.v2.service.PetStoreService;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DependencyInjectionTest {
    @Test
    public void test() {
        BeanIocContainer iocContainer = new BeanIocContainer("petstore-v2.xml");
        PetStoreService petStore = (PetStoreService) iocContainer.getBean("petStore");

        assertNotNull(petStore.getAccount());
        assertNotNull(petStore.getItem());

        assertTrue(petStore.getAccount() instanceof Account);
        assertTrue(petStore.getItem() instanceof Item);
    }
}
