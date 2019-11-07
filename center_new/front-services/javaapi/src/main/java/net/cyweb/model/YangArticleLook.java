package net.cyweb.model;

import jnr.ffi.annotations.In;
import org.web3j.abi.datatypes.Int;

import javax.persistence.Table;
import java.util.Date;

@Table(name = "yang_article_look")
public class YangArticleLook extends BaseEntity{
    private Integer id;
    private Integer articleId;
    private Date CreateAt;
    private Integer memberId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Date getCreateAt() {
        return CreateAt;
    }

    public void setCreateAt(Date createAt) {
        CreateAt = createAt;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }
}
