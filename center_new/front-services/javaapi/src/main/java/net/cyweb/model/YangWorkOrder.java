package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class YangWorkOrder extends BaseEntity{
    private String id;
    private String title;
    private String content;
    private String pic1;
    private String pic2;
    private String pic3;
    private Date createdTime;
    private String memberName;
    private Integer memberId;
    private String replyContent;
    private String replyUserId;
    private String replyUserName;
    private Date replyTime;
    private String isReply;
    private Integer type;
}