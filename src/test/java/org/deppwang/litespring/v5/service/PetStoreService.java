package org.deppwang.litespring.v5.service;

import org.deppwang.litespring.v4.dao.AccountDao;
import org.deppwang.litespring.v4.stereotype.Autowired;
import org.deppwang.litespring.v4.stereotype.Component;

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
