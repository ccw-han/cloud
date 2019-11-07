package com.example.orderservice.service;

import com.example.orderservice.entity.Item;
import com.example.orderservice.feign.ItemFeignClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ItemService {

    // Spring框架对RESTful方式的http请求做了封装，来简化操作
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ItemFeignClient itemFeignClient;

    public Item queryItemById(Long id) {
//        return this.restTemplate.getForObject("http://127.0.0.1:8081/item/"
//                + id, Item.class);
        // 该方法走eureka注册中心调用(去注册中心根据app-item查找服务，这种方式必须先开启负载均衡@LoadBalanced)
        String itemUrl = "http://app-item/item/{id}";
        Item result = restTemplate.getForObject(itemUrl, Item.class, id);
        System.out.println("订单系统调用商品服务,result:" + result);
        return result;

    }

    /**
     * 进行容错处理
     * fallbackMethod的方法参数个数类型要和原方法一致
     *1包裹请求，独立线程运行
     * 2跳闸机制。一定值时，自动或者手动跳闸
     * 3资源隔离 每一个资源有小的线程池，一旦满了，请求被拒绝，而不是排队等候
     * 4监控 监控成功或者失败拒绝的请求
     * 5回退机制 自定义一个方法
     * 6自我修复：进入半开状态
     * @param id
     * @return
     */
    @HystrixCommand(fallbackMethod = "queryItemByIdFallbackMethod")
    public Item queryItemById3(Long id) {
//        String itemUrl = "http://app-item/item/{id}";
//        Item result = restTemplate.getForObject(itemUrl, Item.class, id);
        Item result = itemFeignClient.queryItemById(id);
        System.out.println("===========HystrixCommand queryItemById-线程池名称：" + Thread.currentThread().getName() + "订单系统调用商品服务,result:" + result);
        return result;
    }

    /**
     * 请求失败执行的方法
     * fallbackMethod的方法参数个数类型要和原方法一致
     *设置了专门的回调类，此处删除
     * @param id
     * @return
     */
//    public Item queryItemByIdFallbackMethod(Long id) {
//        return new Item(id, "查询商品信息出错!", null, null, null);
//    }


}

