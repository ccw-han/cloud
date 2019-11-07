/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.cyweb.model;

/**
 * 文章内容表Entity
 * @author zhoutc
 */
public class FunArticleContent extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	private String title;		// title
	private String content;		// content
	private String language;		// 1中文 2韩文 3英语
	
	public FunArticleContent() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
}