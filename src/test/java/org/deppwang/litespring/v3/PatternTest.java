package org.deppwang.litespring.v3;

import org.deppwang.litespring.v3.entry.Account;
import org.deppwang.litespring.v3.entry.Item;
import org.deppwang.litespring.v3.service.PetStoreService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PatternTest {
    @Test
    public void test() {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("petstore-v3.xml");
//        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("petstore-v3.xml"));

        BeanFactory ctx = new ClassPathXmlApplicationContext("petstore-v3.xml");
        XmlBeanFactory factory = new XmlBeanFactory("petstore-v3.xml");
        PetStoreService petStore = (PetStoreService) ctx.getBean("petStore");

        assertNotNull(petStore.getAccount());
        petStore.getAccount().getName();

        assertNotNull(petStore.getItem());

        assertTrue(petStore.getAccount() instanceof Account);
        assertTrue(petStore.getItem() instanceof Item);
    }
}
