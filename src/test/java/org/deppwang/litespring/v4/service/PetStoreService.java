package org.deppwang.litespring.v4.service;

import org.deppwang.litespring.v4.dao.AccountDao;
import org.deppwang.litespring.v4.stereotype.Autowired;
import org.deppwang.litespring.v4.stereotype.Component;

/**
 * @Service 注解中包含 @Component
 * petStore 为 PetStoreService 在容器中的 id
 */
@Component(value = "petStore")
public class PetStoreService {
    @Autowired
    private AccountDao accountDao;

    String name = "petStore";

    public AccountDao getAccountDao() {
        return accountDao;
    }

    public String getName() {
        return name;
    }
}
