package org.deppwang.litespring.v2.service;

import org.deppwang.litespring.v2.dao.AccountDao;

public class PetStoreService {

    private AccountDao accountDao;

    // 1、使用构造方法
    public PetStoreService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    // 2、使用 Setter 方法
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public AccountDao getAccountDao() {
        return accountDao;
    }
}
