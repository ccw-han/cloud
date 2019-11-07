package net.cyweb.controller;

import net.cyweb.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping(value = "testRedis")
public class TestRedisController {

    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "testWriteAndRead")
    public void testWriteAndRead(){
        Integer maxTimes = 50000000;
        Integer maxTimes2 = 50000000;

        System.out.println("塞入数据");
        while (maxTimes-- > 0){
            redisService.zAdd("test",new Random().nextInt(1000)+1,new Random().nextInt(1000)+1);
            if(maxTimes%10000 == 0){
                System.out.println(maxTimes);
            }

        }
        System.out.println("开始时间");
        long startTime = System.currentTimeMillis();
        Set<ZSetOperations.TypedTuple<Object>> set = redisService.rangeByScoreWithScores("test", 0, Double.MAX_VALUE);
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple) iterator.next();
        }
        long endTime = System.currentTimeMillis();

        System.out.println( (endTime - startTime) );
        System.out.println("结束时间");
    }

}
