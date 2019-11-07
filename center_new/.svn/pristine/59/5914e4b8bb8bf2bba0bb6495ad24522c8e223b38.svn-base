package net.cyweb.model;

import com.sun.tools.corba.se.idl.InterfaceGen;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="yang_areas")
public class YangArea extends BaseEntity{
    @Id
    @Column(name = "area_id")
    private Integer areaId;
    @Column(name = "parent_id")
    private Integer parentId;
    @Column(name = "area_name")
    private String areaName;
    @Column(name = "area_type")
    private String areaType;

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }
}
