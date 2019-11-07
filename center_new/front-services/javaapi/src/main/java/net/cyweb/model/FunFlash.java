/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.cyweb.model;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 幻灯表Entity
 * @author 瓶子
 * @version 2018-04-25
 */
@Table(name = "yang_flash")
public class FunFlash extends BaseEntity {
	
	@Id
	private String flashId;		// flash_id
	private String title;		// 标题
	private String pic;		// pic
	private String jumpUrl;		// 跳转地址
	private String sort;		// 排序
	private String type;		// type
	private String addTime;		// add_time
	private String status;		// status
	private String pickr;		// 韩语图片地址
	private String picus;		// 英文地址
	private String delFlag;
	@Transient
	private byte[] picByte;
	@Transient
	private byte[] pickrByte;
	@Transient
	private byte[] picusByte;

	public FunFlash() {
		super();
	}

	public String getFlashId() {
		return flashId;
	}

	public void setFlashId(String flashId) {
		this.flashId = flashId;
	}
	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}
	

	public String getJumpUrl() {
		return jumpUrl;
	}

	public void setJumpUrl(String jumpUrl) {
		this.jumpUrl = jumpUrl;
	}
	
	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getPickr() {
		return pickr;
	}

	public void setPickr(String pickr) {
		this.pickr = pickr;
	}
	
	public String getPicus() {
		return picus;
	}

	public void setPicus(String picus) {
		this.picus = picus;
	}

	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}

	public byte[] getPicByte() {
		return picByte;
	}

	public void setPicByte(byte[] picByte) {
		this.picByte = picByte;
	}

	public byte[] getPickrByte() {
		return pickrByte;
	}

	public void setPickrByte(byte[] pickrByte) {
		this.pickrByte = pickrByte;
	}

	public byte[] getPicusByte() {
		return picusByte;
	}

	public void setPicusByte(byte[] picusByte) {
		this.picusByte = picusByte;
	}
}