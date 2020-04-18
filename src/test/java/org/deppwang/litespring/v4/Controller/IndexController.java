package org.deppwang.litespring.v4.Controller;

import org.deppwang.litespring.v4.service.PetStoreService;
import org.deppwang.litespring.v4.stereotype.Autowired;
import org.deppwang.litespring.v4.stereotype.Component;

/**
 * @Controller 中包含 @Component
 */
@Component
public class IndexController {
    @Autowired
    PetStoreService petStore;

    public void index(String request) {
        System.out.println(request);
    }
}
