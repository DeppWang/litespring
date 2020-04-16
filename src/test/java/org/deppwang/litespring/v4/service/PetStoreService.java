package org.deppwang.litespring.v4.service;

import org.deppwang.litespring.v4.entry.Account;
import org.deppwang.litespring.v4.entry.Item;
import org.deppwang.litespring.v4.stereotype.Autowired;
import org.deppwang.litespring.v4.stereotype.Component;

@Component(value = "petStore")
public class PetStoreService {
    @Autowired
    private Account account;

    @Autowired
    private Item item;

    public Account getAccount() {
        return account;
    }

    public Item getItem() {
        return item;
    }
}
