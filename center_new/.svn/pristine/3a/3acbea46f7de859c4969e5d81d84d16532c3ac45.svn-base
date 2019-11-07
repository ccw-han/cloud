package net.cyweb.feignClient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import net.cyweb.model.Result;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "user-server",fallback = UserServiceHystric.class)
public interface UserService {
    @RequestMapping(value = "/user/list",method = RequestMethod.GET)
    Result listFromSerice();
}
