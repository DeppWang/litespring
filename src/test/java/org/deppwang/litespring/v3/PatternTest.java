package org.deppwang.litespring.v3;

import org.deppwang.litespring.v3.dao.AccountDao;
import org.deppwang.litespring.v3.service.PetStoreService;
import org.junit.Test;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PatternTest {
    @Test
    public void test() {
        // 使用 Spring 框架的 ClassPathXmlApplicationContext 和 XmlBeanFactory
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("petstore-v3.xml");
//        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("petstore-v3.xml"));

//        XmlBeanFactory factory = new XmlBeanFactory("petstore-v3.xml");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("petstore-v3.xml");
        PetStoreService petStore = (PetStoreService) ctx.getBean("petStore");

        assertNotNull(petStore.getAccountDao());
        petStore.getAccountDao().getName();

        assertTrue(petStore.getAccountDao() instanceof AccountDao);
    }
}
