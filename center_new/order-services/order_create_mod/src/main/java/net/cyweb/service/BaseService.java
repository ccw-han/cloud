package net.cyweb.service;

import com.github.pagehelper.PageHelper;
import net.cyweb.model.BaseEntity;
import net.cyweb.model.YangTibi;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by wuhongbing on 2017/12/1.
 */

public abstract class BaseService<T extends BaseEntity> {

    @Autowired
    protected Mapper<T> mapper;

    public int save(T entity){
        return mapper.insert(entity);
    }
    public int saveSelective(T entity){
        return mapper.insertSelective(entity);
    }


    public int delete(T entity){
        return mapper.deleteByPrimaryKey(entity);
    }

    public int update(T t){
        return mapper.updateByPrimaryKey(t);

    }

    public int updateByExampleSelective(T t,Example e)
    {
        return mapper.updateByExampleSelective(t,e);
    }

    public int updateByPrimaryKeySelective(T t){
        return mapper.updateByPrimaryKeySelective(t);
    }

    public T selectOne(T t){
        return mapper.selectOne(t);
    }

    public List<T> select(T t){
        return mapper.select(t);
    }

    public List<T> selectByExample(Example example)
    {
        return mapper.selectByExample(example);
    }



    public int selectCount(T t){
        return mapper.selectCount(t);
    }

    public List<T> selectAll(){
        return mapper.selectAll();
    }

    /**
     * 单表分页查询
     *
     * @param t
     * @return
     */
    public List<T> selectPage(T t){
        PageHelper.startPage(t.getPageNum(), t.getPageSize());
        //Spring4支持泛型注入
        return mapper.select(null);
    }
}
