package org.deppwang.litespring.v3.service;

import org.deppwang.litespring.v3.dao.AccountDao;

public class PetStoreService {
    private AccountDao accountDao;

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public AccountDao getAccountDao() {
        return accountDao;
    }

}
