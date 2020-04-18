package org.deppwang.litespring.v2.service;

import org.deppwang.litespring.v2.dao.AccountDao;

public class PetStoreService {


    // 1、使用 Setter 方法
    AccountDao accountDao;

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public AccountDao getAccountDao() {
        return accountDao;
    }


    // 2、使用构造函数
//    AccountDao accountDao;
//
//    public PetStoreService(AccountDao accountDao) {
//        this.accountDao = accountDao;
//    }

}
