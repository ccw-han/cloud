package net.cyweb;

import com.alibaba.fastjson.JSON;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import net.cyweb.model.YangConfig;
import net.cyweb.model.YangTrade;
import net.cyweb.model.modelExt.YangTradeExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@RunWith(SpringRunner.class)
@Profile(value = "dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MondbTest {


    @Autowired
    private MongoTemplate mongoTemplate;


    @Test
    public void testContent()
    {

        DB mongoDatabase =  mongoTemplate.getDb();
        System.out.println(mongoDatabase);

    }

    @Test
    public void inserData()
    {
        YangTradeExt yangTrade = new YangTradeExt();
        int i = 0;
        long s;
        long a = System.currentTimeMillis();
        while (i++ <= 100)
        {
            s  = System.currentTimeMillis()/1000;
            yangTrade.setAddTime(s);
            yangTrade.setMoney(BigDecimal.valueOf(i+100));
            yangTrade.setNum(BigDecimal.valueOf(i));
            yangTrade.setNumD(Double.valueOf(i));
            yangTrade.setPriceD(Double.valueOf(i+100));
            mongoTemplate.insert(yangTrade,"yangTrade");
        }
        long d = System.currentTimeMillis();

        System.out.println("成功 耗时"+(d - a)+"ms");


    }

    @Test
    public void selectData()
    {
        Query query = new Query();
//        query.addCriteria(Criteria.where("key").gt("2"));
        query.limit(1);
        Sort sort = new Sort("tradeId asc");

        query = query.with(sort);
        YangTradeExt yangConfigs = mongoTemplate.findOne(query,YangTradeExt.class,"yangTrade");
        System.out.println(yangConfigs);
    }

    /**
     * 聚合
     */
    @Test
    public void aggregateData()
    {

        long d = System.currentTimeMillis();
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group().min("priceD").as("minMoney").
                        sum("numD").as("total").
                        sum(ArithmeticOperators.Multiply.valueOf("numD").
                        multiplyBy(ArithmeticOperators.Multiply.valueOf("priceD"))).as("totalMoney")


        );
        AggregationResults<HashMap> aggregate  =  mongoTemplate.aggregate(aggregation, "yangTrade",HashMap.class);

        System.out.println(aggregation.toString());
        for (Iterator<HashMap> iterator = aggregate.iterator(); iterator.hasNext(); ) {
            HashMap obj = iterator.next();
            System.out.println(obj);
        }

        long a = System.currentTimeMillis();

        System.out.println("成功 耗时"+(a - d)+"ms");

    }

    @Test
    public void drop()
    {
        Query query = new Query();
        query.addCriteria(Criteria.where("key").gt("4"));
        mongoTemplate.remove(query,YangConfig.class,"yangconfig");
    }




}
