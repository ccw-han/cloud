package net.cyweb.service;

import net.cyweb.mapper.YangTibiMapper;
import net.cyweb.mapper.YangTibiNewMapper;
import net.cyweb.model.YangTibi;
import net.cyweb.model.YangTibiNew;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YangTibiNewService extends BaseService<YangTibiNew> {


    @Autowired
    YangTibiNewMapper yangTibiNewMapper;




}
