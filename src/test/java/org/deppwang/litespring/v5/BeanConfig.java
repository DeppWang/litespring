package org.deppwang.litespring.v5;

import org.deppwang.litespring.v5.dao.AccountDao;
import org.deppwang.litespring.v5.service.PetStoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class BeanConfig {
    @Bean
    PetStoreService setPet() {
        return new PetStoreService();
    }

    @Bean
    AccountDao setAccount() {
        return new AccountDao();
    }

    @Bean
    public ExecutorService sendMessageExecutor() {
//        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
//                .setNameFormat("task-%d").build();

        ExecutorService executor = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), new ThreadPoolExecutor.AbortPolicy());

        return executor;
    }
}
