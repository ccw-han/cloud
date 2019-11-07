package net.cyweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Scope("prototype")
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 写入缓存
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 写入缓存设置时效时间
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime ,TimeUnit timeUnit) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, timeUnit);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 批量删除对应的value
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除key
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0){
            redisTemplate.delete(keys);
        }
    }
    /**
     * 删除对应的value
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }
    /**
     * 判断缓存中是否有对应的value
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }
    /**
     * 读取缓存
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 哈希 添加
     * @param key
     * @param hashKey
     * @param value
     */
    public void hmSet(String key, Object hashKey, Object value){
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(key,hashKey,value);
    }

    /**
     * 哈希获取数据
     * @param key
     * @param hashKey
     * @return
     */
    public Object hmGet(String key, Object hashKey){
        HashOperations<String, Object, Object>  hash = redisTemplate.opsForHash();
        return hash.get(key,hashKey);
    }

    /**
     * 列表添加
     * @param k
     * @param v
     */
    public void lPush(String k,Object v){
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.rightPush(k,v);
    }

    /**
     * 列表添加 批量
     * @param k
     * @param values
     */
    public void lPushAll(String k, Collection values){
        ListOperations<String, Object> list = redisTemplate.opsForList();
        try {
            if(null != values && values.size() > 0 )
            {
                list.rightPushAll(k,values);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * 列表添加
     * @param k
     * @param v
     */
    public void rPush(String k,Object v){
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.rightPush(k,v);
    }

    /**
     * 列表获取
     * @param k
     * @param l
     * @param l1
     * @return
     */
    public List<Object> lRange(String k, long l, long l1){
        ListOperations<String, Object> list = redisTemplate.opsForList();
        return list.range(k,l,l1);
    }

    /**
     * 集合添加
     * @param key
     * @param value
     */
    public void add(String key,Object value){
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        set.add(key,value);
    }

    /**
     * 集合获取
     * @param key
     * @return
     */
    public Set<Object> setMembers(String key){
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        return set.members(key);
    }

    /**
     * 有序集合添加
     * @param key
     * @param value
     * @param scoure
     */
    public void zAdd(String key,Object value,double scoure){
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        zset.add(key,value,scoure);
    }


    /**
     * 集合删除元素用
     * @param key
     * @param value
     */
    public long zremove(String key,Object value)
    {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return  zset.remove(key,value,value);
    }

    public void zclear(String key)
    {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.trim(key,1,0);
    }


    public long zsize(String key)
    {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.size(key);

    }




    /**
     * 有序集合获取
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<Object> rangeByScore(String key,double scoure,double scoure1){
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.rangeByScore(key, scoure, scoure1);
    }


    /**
     * 正序 从小到达
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<ZSetOperations.TypedTuple<Object>> rangeByScoreWithScores(String key,double scoure,double scoure1){
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.rangeByScoreWithScores(key, scoure, scoure1);
    }

    /**
     * 倒叙 从大到小
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String key,double scoure,double scoure1){
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.reverseRangeByScoreWithScores(key, scoure, scoure1);
    }




    /**
     * 删除范围内的数据
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public  Long removeRangeByScore(String key,double scoure,double scoure1)
    {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.removeRangeByScore(key, scoure, scoure1);
    }

    /*list lpop*/
    public Object lpop(String key){
        ListOperations opsForList = redisTemplate.opsForList();
        return opsForList.leftPop(key);
    }

    /*list lpop*/
    public Long listSize(String key){
        ListOperations opsForList = redisTemplate.opsForList();
        return opsForList.size(key);
    }

    /*开启事务*/
    public void multi(){
        redisTemplate.multi();
    }

    /*提交事务*/
    public void exec(){
        redisTemplate.execute(sessionCallback);
//        redisTemplate.exec();
    }

    //支持事务
    public void init(){
        redisTemplate.setEnableTransactionSupport(true);
    }


    /*取消事务*/
    public void discard(){
        redisTemplate.discard();
    }




    SessionCallback sessionCallback = new SessionCallback() {
        @Override
        public Object execute(RedisOperations redisOperations) throws DataAccessException {
            redisOperations.multi();
            return redisOperations.exec();
        }
    };

}
