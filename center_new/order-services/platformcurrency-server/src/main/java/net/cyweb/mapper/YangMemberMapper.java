package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangMember;
import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.HashMap;


@Mapper
public interface YangMemberMapper extends TkMapper<YangMember> {

//    call assets(3211,'0',125.001,'dec',56.89,'dec');

//    call assets_one2one(
// 3211,'40',125.001,'asc',56.89,'asc',
//         0,'40',125.001,'asc',102,'dec'
//    );


    int assets(HashMap pama);

    int orderConfirm(HashMap pama);


}