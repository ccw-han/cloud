package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.CmsCategoryMapper;
import net.cyweb.mapper.FunArticleMapper;
import net.cyweb.mapper.YangArticleLookMapper;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.*;

@Service
public class YangArticleService {
    @Autowired
    private FunArticleMapper funArticleMapper;

    @Autowired
    private YangArticleLookMapper yangArticleLookMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private CmsCategoryMapper cmsCategoryMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 文章列表  1 最新公告 2 新币上线
     *
     * @param language
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    public Result getArticleList(String language, String type, String searchText, int page, int pageSize) {
        Result result = new Result();
        Map resultMap = new HashMap();
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            if (StringUtils.isNotEmpty(type)) {
                param.put("type", type);
                if (CoinConst.ARTICLE_PTGG.equals(type)) {
                    param.put("keywords", CoinConst.ARTICLE_PTGG_KEYWORDS);
                }
                if (CoinConst.ARTICLE_XBSX.equals(type)) {
                    param.put("keywords", CoinConst.ARTICLE_XBSX_KEYWORDS);
                }
                if (CoinConst.ARTICLE_BZJS.equals(type)) {
                    param.put("keywords", CoinConst.ARTICLE_BZJS_KEYWORDS);
                }
            } else {
                param.put("keywords", CoinConst.ARTICLE_PTGG_KEYWORDS);
            }
            List<Map> positionMap = funArticleMapper.getGongGaoType(param);


            param.put("positionId", positionMap);
            //中文和 繁体 取 中文
            if (CoinConst.FONT_LANGUAGE_CN.equals(language) || CoinConst.FONT_LANGUAGE_FT.equals(language)) {
                param.put("language", CoinConst.FONT_LANGUAGE_CN);
            }
            //英文和韩语 取   英文
            if (CoinConst.FONT_LANGUAGE_EN.equals(language) || CoinConst.FONT_LANGUAGE_KR.equals(language)) {
                param.put("language", CoinConst.FONT_LANGUAGE_EN);
            }
            param.put("start", (page - 1) * pageSize);
            param.put("end", pageSize);
            if (StringUtils.isNotEmpty(searchText)) {
                param.put("searchText", searchText);
            }
            List<Map> resultList = funArticleMapper.getArticleList(param);
            if (resultList.size() > 0) {
                Map countMap = funArticleMapper.countArticleList(param);
                resultMap.put("resultList", resultList);
                resultMap.put("total", (long) countMap.get("total"));
                result.setCode(Result.Code.SUCCESS);
                result.setData(resultMap);
                result.setMsg("查询成功");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 文章列表  1 最新公告 2 新币上线
     *
     * @param language
     * @param type
     * @return
     */
    public Result getArticleByArticleId(String type, String searchText, Map<String, String> map) {
        Result result = new Result();
        Map resultMap = new HashMap();
        if (StringUtils.isBlank(map.get("articleId"))) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        if (StringUtils.isBlank(map.get("language"))) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String id = map.get("articleId");
        String language = map.get("language");
        if (StringUtils.isNotBlank(map.get("accessToken"))) {
            String accessToken = map.get("accessToken");
            String token = CoinConst.TOKENKEYTOKEN + accessToken;
            //验证token
            Object memberId = redisService.get(token);
            if (memberId == null) {
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return result;
            }
        }
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            if (StringUtils.isNotEmpty(type)) {
                param.put("type", type);
                if (CoinConst.ARTICLE_PTGG.equals(type)) {
                    param.put("keywords", CoinConst.ARTICLE_PTGG_KEYWORDS);
                }
                if (CoinConst.ARTICLE_XBSX.equals(type)) {
                    param.put("keywords", CoinConst.ARTICLE_XBSX_KEYWORDS);
                }
                if (CoinConst.ARTICLE_BZJS.equals(type)) {
                    param.put("keywords", CoinConst.ARTICLE_BZJS_KEYWORDS);
                }
            } else {
                param.put("keywords", CoinConst.ARTICLE_PTGG_KEYWORDS);
            }
            List<Map> positionMap = funArticleMapper.getGongGaoType(param);


            param.put("positionId", positionMap);
            //中文和 繁体 取 中文
            if (CoinConst.FONT_LANGUAGE_CN.equals(language) || CoinConst.FONT_LANGUAGE_FT.equals(language)) {
                param.put("language", CoinConst.FONT_LANGUAGE_CN);
            }
            //英文和韩语 取   英文
            if (CoinConst.FONT_LANGUAGE_EN.equals(language) || CoinConst.FONT_LANGUAGE_KR.equals(language)) {
                param.put("language", CoinConst.FONT_LANGUAGE_EN);
            }
            if (StringUtils.isNotEmpty(searchText)) {
                param.put("searchText", searchText);
            }
            param.put("articleId", id);
            List<Map> resultList = funArticleMapper.getArticleByArticleId(param);
            if (resultList.size() > 0) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(resultList);
                result.setMsg("获取公告详情成功");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("获取公告详情失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 文章详情
     *
     * @return
     */
    public Result getArticleDetail(String language, String articleId, String accessToken) {
        Result result = new Result();
        try {
            Map param = new HashMap();
            Map resultMap = new HashMap();
            param.put("articleId", articleId);
            param.put("language", language);
            //获取文章详情
            List<Map> detailMap = funArticleMapper.getArticleDetail(param);
            if (detailMap == null || detailMap.size() == 0) {
                Map a = new HashMap();
                a.put("articleId", articleId);
                List<Map> b = funArticleMapper.getArticleDetail(a);
                param.put("positionId", b.get(0).get("positionId"));
                resultMap.put("articleDetail", new HashMap());
            } else {
                param.put("positionId", detailMap.get(0).get("positionId"));
                resultMap.put("articleDetail", detailMap.get(0));
            }
            //验证用户信息
            YangMemberToken yangMemberToken = new YangMemberToken();
            yangMemberToken.setAccessToken(accessToken);
            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
            if (yangMemberSelf != null) {
                //获取用户最近浏览记录
                Map memberMap = new HashMap();
                memberMap.put("memberId", yangMemberSelf.getMemberId());
                List<Map> articleLookList = yangArticleLookMapper.getLookList(memberMap);
                resultMap.put("lookList", articleLookList);
                //记录当前这条 浏览记录
                YangArticleLook yangArticleLook = new YangArticleLook();
                yangArticleLook.setArticleId(Integer.valueOf(articleId));
                yangArticleLook.setCreateAt(new Date());
                yangArticleLook.setMemberId(yangMemberSelf.getMemberId());
                yangArticleLookMapper.insert(yangArticleLook);
            } else {
                resultMap.put("lookList", new ArrayList<Map>());
            }
            //获取相关文章5条
            param.remove("articleId");
            List<Map> relativeArticList = funArticleMapper.getRelativeArticList(param);
            List<Map> relativeArticRandomList = funArticleMapper.getRelativeArticListRandom(param);
            resultMap.put("relativeList", relativeArticList);
            resultMap.put("relativeArticRandomList", relativeArticRandomList);

            result.setCode(Result.Code.SUCCESS);
            result.setData(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getCategory() {
        Result result = new Result();
        try {
            Example example = new Example(CmsCategory.class);
            example.setOrderByClause("sort asc");
            example.createCriteria().andEqualTo("delFlag", CoinConst.DEL_FLAG_NO).andNotEqualTo("id", "1");
            List<CmsCategory> list = cmsCategoryMapper.selectByExample(example);
            List<FontArticleResponse> resultList = new ArrayList<FontArticleResponse>();
            for (CmsCategory cmsCategory : list) {
                if (cmsCategory.getModule().equals(CoinConst.CATEGORY_NODE)) {
                    FontArticleResponse fontArticleResponse = new FontArticleResponse();
                    fontArticleResponse.setId(cmsCategory.getId());
                    fontArticleResponse.setParentId(cmsCategory.getParentId());
                    fontArticleResponse.setParentIds(cmsCategory.getParentIds());
                    fontArticleResponse.setModule(cmsCategory.getModule());
                    fontArticleResponse.setName(cmsCategory.getName());
                    fontArticleResponse.setKeyWords(cmsCategory.getKeywords());
                    fontArticleResponse.setSort(cmsCategory.getSort().toString());
                    fontArticleResponse.setHref(cmsCategory.getHref());
                    fontArticleResponse.setChildren(new ArrayList<FontArticleResponse>());
                    resultList.add(fontArticleResponse);
                }
            }
            for (CmsCategory cmsCategory : list) {
                for (FontArticleResponse fontArticleResponse : resultList) {
                    if (fontArticleResponse.getId().equals(cmsCategory.getParentId())) {
                        //文章模式 直接填写 url
                        FontArticleResponse fontArticleResponsea = new FontArticleResponse();
                        if (!StringUtils.isNotEmpty(cmsCategory.getHref())) {
                            //单一文章模式  获取 文章列表第一个
                            if (CoinConst.CATEGORY_SINGLE_ARTICLE.equals(cmsCategory.getModule())) {
                                Map map = new HashMap();
                                List<Map> positionIdList = new ArrayList<Map>();
                                map.put("positionId", fontArticleResponse.getId());
                                Map positionMap = new HashMap();
                                positionMap.put("positionId", cmsCategory.getId());
                                positionIdList.add(positionMap);
                                map.put("positionId", positionIdList);
                                map.put("start", 0);
                                map.put("end", 1);
                                List<Map> singleList = funArticleMapper.getArticleList(map);
                                if (singleList != null && singleList.size() > 0) {
                                    fontArticleResponsea.setHref(CoinConst.SINGLE_ARTICLE_PAGE_URL + "?" + singleList.get(0).get("articleId"));
                                } else {
                                    fontArticleResponsea.setHref("#");
                                }
                            } else if (CoinConst.CATEGORY_LINK.equals(cmsCategory.getModule())) {
                                Map map = new HashMap();
                                map.put("delFlag", CoinConst.DEL_FLAG_NO);
                                map.put("positionId", cmsCategory.getId());
                                map = funArticleMapper.getLinkeList(map);
                                if (map != null && StringUtils.isNotEmpty((String) map.get("href"))) {
                                    fontArticleResponsea.setHref((String) map.get("href"));
                                } else {
                                    fontArticleResponsea.setHref("#");
                                }
                            } else {
                                fontArticleResponsea.setHref("#");
                            }
                        } else {
                            fontArticleResponsea.setHref(cmsCategory.getHref());
                        }
                        fontArticleResponsea.setId(cmsCategory.getId());
                        fontArticleResponsea.setParentId(cmsCategory.getParentId());
                        fontArticleResponsea.setParentIds(cmsCategory.getParentIds());
                        fontArticleResponsea.setModule(cmsCategory.getModule());
                        fontArticleResponsea.setName(cmsCategory.getName());
                        fontArticleResponsea.setKeyWords(cmsCategory.getKeywords());
                        fontArticleResponsea.setSort(cmsCategory.getSort().toString());
                        fontArticleResponsea.setChildren(new ArrayList<FontArticleResponse>());
                        fontArticleResponse.getChildren().add(fontArticleResponsea);
                    }
                }
            }
            result.setData(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
