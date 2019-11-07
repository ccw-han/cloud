package net.cyweb.model;

import javax.persistence.*;

@Table(name = "yang_article_content")
public class YangArticleContent extends BaseEntity {
    @Id
    private Integer id;

    private String title;

    /**
     * 1中文 2韩文 3英语
     */
    private String language;

    private String content;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取1中文 2韩文 3英语
     *
     * @return language - 1中文 2韩文 3英语
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 设置1中文 2韩文 3英语
     *
     * @param language 1中文 2韩文 3英语
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }
}