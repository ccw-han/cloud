package net.cyweb.model;

/**
 * Created by wuhongbing on 2017/11/29.
 */

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * 基础信息
 *
 * @author wuhongbing
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)

public abstract class BaseEntity {
//    @Id
//    @Column(name = "id")
//    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "select replace(uuid(),'-','')")
//    private String id;

    @Transient
//    @ApiParam(value = "当前页", required = true)
    @Min(value = 1,message = "当前页码不能小于1")
    private Integer pageNum = 1;

    @Transient
//    @ApiParam(value = "查询条数", required = true)
    @Max(value = 500,message = "查询条数不能超过500")
    @Min(value = 1,message = "查询条数不能小于1")
    private Integer pageSize = 20;

//    /**
//     * 创建时间
//     */
//    @Column(name = "create_date")
//    private Date createDate;
//
//    /**
//     * 创建者
//     */
//    @Column(name = "create_by")
//    private String createBy;
//
//    /**
//     * 删除位标记
//     */
//    @Column(name = "del_flag")
//    private Boolean delFlag;
//
//    /**
//     * 备注
//     */
//    private String remarks;
}
