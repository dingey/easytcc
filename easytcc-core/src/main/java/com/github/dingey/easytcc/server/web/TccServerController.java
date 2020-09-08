package com.github.dingey.easytcc.server.web;

import com.github.dingey.easytcc.core.Group;
import com.github.dingey.easytcc.server.handler.EasyTccHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author d
 */
@RestController
public class TccServerController {
    @Resource
    private EasyTccHandler tccHandler;

    @PostMapping(path = "/easy/tcc/trying", produces = "application/json;charset=UTF-8")
    public Integer trying(@RequestBody Group group) {
        return tccHandler.trying(group);
    }
}
