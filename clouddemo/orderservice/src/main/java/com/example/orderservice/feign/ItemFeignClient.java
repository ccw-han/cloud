package com.example.orderservice.feign;

import com.example.orderservice.entity.Item;
import com.example.orderservice.hystrixfallback.ItemServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 申明这是一个Feign客户端，并且指明服务id
 * 实际开发中ItemFeignClient一般直接继承(extends)服务提供方的接口以避免代码重复（例如Item工程会以jar包的形式提供ItemService接口）
 *
 * @author Evan
 */
@FeignClient(value = "app-item", fallback = ItemServiceFallback.class)
public interface ItemFeignClient {
    /**
     * 这里定义了类似于SpringMVC用法的方法，就可以进行RESTful方式的调用了
     * 一切和mvc一样
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/item/{id}", method = RequestMethod.GET)
    Item queryItemById(@PathVariable("id") Long id);
}
