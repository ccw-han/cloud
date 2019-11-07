package net.cyweb.service;

import net.cyweb.mapper.YangTibiMapper;
import net.cyweb.model.YangTibi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class YangTibiService extends BaseService<YangTibi> {


    @Autowired
    YangTibiMapper tibiMapper;




}
