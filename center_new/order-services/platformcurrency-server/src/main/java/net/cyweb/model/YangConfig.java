package net.cyweb.model;

import lombok.Data;

import javax.persistence.*;

@Table(name = "yang_config")
@Data
public class YangConfig extends BaseEntity {
    @Column(name = "`key`")
    private String key;

    @Column(name = "`value`")
    private String value;


}