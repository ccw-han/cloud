package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberMinginfos {
    private BigDecimal lastMine;
    private BigDecimal totalMine;
    private BigDecimal cardMine;
    private BigDecimal lastLock;
    private BigDecimal totalLock;
    private BigDecimal pool;
}