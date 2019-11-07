package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="yang_nationality")
public class YangNationality {
    @Id
    @Column(name="id")
    private Integer id;

    @Column(name="name_en")
    private String nameEn;

    @Column(name="show_cn")
    private String showCn;
}
