package net.cyweb.task;

import cyweb.utils.CoinConst;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
@EnableAsync
@Service
public class funtMiningInfoTask {

    @Autowired
    private RedisService redisService;
    @Autowired
    private YangMemberService yangMemberService;

    //funt全局缓存
    //每10分钟执行一次
//    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional
    public void dealMiningInfo()
    {
        System.out.println("全局 funt 缓存数据《》《》《》《");
        yangMemberService.dealMiningInfo();
    }

}
