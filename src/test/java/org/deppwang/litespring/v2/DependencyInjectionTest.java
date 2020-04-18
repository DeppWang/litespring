package org.deppwang.litespring.v2;

import org.deppwang.litespring.v2.dao.AccountDao;
import org.deppwang.litespring.v2.service.PetStoreService;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DependencyInjectionTest {
    @Test
    public void test() {
        // 正常的依赖注入
//        PetStoreService petStore = new PetStoreService();
//        AccountDao account = new AccountDao();
//        petStore.setAccountDao(account);

        BeanIocContainer iocContainer = new BeanIocContainer("petstore-v2.xml");
        PetStoreService petStore = (PetStoreService) iocContainer.getBean("petStore");

        assertNotNull(petStore.getAccountDao());

        assertTrue(petStore.getAccountDao() instanceof AccountDao);
    }
}
