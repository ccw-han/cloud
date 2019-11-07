package net.cyweb.feignClient;

import net.cyweb.message.CodeMsg;
import net.cyweb.model.Result;
import org.springframework.stereotype.Component;

@Component
public class UserServiceHystric implements UserService{
    @Override
    public Result listFromSerice() {
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }
}
