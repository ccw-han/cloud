package net.cyweb.controller;

import net.cyweb.feignClient.UserService;
import net.cyweb.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "test")
public class TestController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = "user/list")
    public Result testUserServiceList(){
        Result result = userService.listFromSerice();
        result.setCode(1);
        result.setMsg("启动成功");
        return result;
    }
}
