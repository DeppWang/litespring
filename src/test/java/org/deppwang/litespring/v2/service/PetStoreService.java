package org.deppwang.litespring.v2.service;

import org.deppwang.litespring.v2.entry.Account;
import org.deppwang.litespring.v2.entry.Item;

public class PetStoreService {
    private Account account;

    private Item item;

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
