package com.example.orderservice.entity;

import java.util.Date;
import java.util.List;

public class Order {

    private String orderId;

    private Long userId;

    private Date createDate;

    private Date updateDate;

    private List<OrderDetail> orderDetails;

    public Order() {

    }

    public Order(String orderId, Long userId, Date createDate, Date updateDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }


    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @Override
    public String toString() {
        return "Order [orderId=" + orderId + ", userId=" + userId
                + ", createDate=" + createDate + ", updateDate=" + updateDate
                + "]";
    }
}

