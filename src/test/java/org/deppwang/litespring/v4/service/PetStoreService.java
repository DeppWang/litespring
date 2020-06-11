package org.deppwang.litespring.v4.service;

import org.deppwang.litespring.v4.dao.AccountDao;
import org.deppwang.litespring.v4.stereotype.Autowired;
import org.deppwang.litespring.v4.stereotype.Component;

/**
 * @Service 注解中包含 @Component
 * 使用 value 可指定 Bean 在容器中的 id，如果不指定即为类名（首字母小写）
 * petStore 为 PetStoreService 在容器中的 id
 */
@Component(value = "petStore")
//@Component("petStore")
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
