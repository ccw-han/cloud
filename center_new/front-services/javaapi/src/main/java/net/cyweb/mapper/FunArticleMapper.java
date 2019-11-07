package net.cyweb.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FunArticleMapper {
    List<Map> getArticleList(Map map);

    Map countArticleList(Map map);

    //相关文章5条
    List<Map> getRelativeArticList(Map map);

    List<Map> getRelativeArticListRandom(Map map);

    List<Map> getGongGaoType(Map map);

    List<Map> getArticleDetail(Map map);

    Map getLinkeList(Map map);

    List<Map> getArticleByArticleId(Map map);
}
