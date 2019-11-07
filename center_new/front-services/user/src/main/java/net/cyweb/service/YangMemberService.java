package net.cyweb.service;

import cyweb.utils.CommonTools;
import cyweb.utils.ErrorCode;
import net.cyweb.exception.EmailNotExistException;
import net.cyweb.exception.EmailRegistRepeatException;
import net.cyweb.exception.MemberIsLocketException;
import net.cyweb.exception.PasswordErrorException;
import net.cyweb.mapper.YangCurrencyUserMapper;
import net.cyweb.mapper.YangMemberMapper;
import net.cyweb.model.YangConfig;
import net.cyweb.model.YangCurrencyUser;
import net.cyweb.model.YangMember;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class YangMemberService extends BaseService<YangMember>{

    @Autowired
    private YangConfigService yangConfigService;

    @Autowired
    private YangMemberMapper memberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    /*用户注册逻辑*/
    public void register(YangMember yangMember) {

        if(!checkEmailRepeat(yangMember)){
            throw new EmailRegistRepeatException("email重复");
        }


        //获取配置
        YangConfig yangConfig = new YangConfig();
        yangConfig.setKey("invit_tay");
        YangConfig yangConfigFind = yangConfigService.selectOne(yangConfig);

        if(null == yangConfigFind || StringUtils.isBlank(yangConfigFind.getValue())){
            throw new RuntimeException("invit_tay 配置不存在");
        }
        yangMember.setInvitTime(Integer.valueOf(yangConfigFind.getValue()) * 86400 + System.currentTimeMillis() / 1000 );


        yangMember.setRegTime(System.currentTimeMillis()/1000);
        yangMember.setShenhestatus(0);

        yangMember.setPwd(DigestUtils.md5Hex(yangMember.getPwd()));
//        yangMember.setPwdtrade(DigestUtils.md5Hex(yangMember.getPwdtrade()));

        saveSelective(yangMember);
    }

    /*检测ecmail是否已经存在*/
    private Boolean checkEmailRepeat(YangMember yangMember){
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());
//        yangMemberQuery.setRegTime(null);
//        yangMemberQuery.setInvitTime(null);
        int selectCount = mapper.selectCount(yangMemberQuery);
        return selectCount == 0;
    }


    /*用户登录*/
    public YangMember login(YangMember yangMember) {
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());
        YangMember yangMemberFind = mapper.selectOne(yangMemberQuery);
        if(null == yangMemberFind){
            throw new EmailNotExistException(ErrorCode.ERROR_EMAIL_NOT_EXIST.getMessage());
        }

        if(!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(yangMember.getPwd()))){
            throw new PasswordErrorException(ErrorCode.ERROR_PASSWORD_ERR.getMessage());
        }

        if(yangMemberFind.getIsLock().intValue() == 1)
        {
            throw new MemberIsLocketException(ErrorCode.MEMBER_HAS_LOCKED.getMessage());
        }



        /*去掉密码*/
        yangMemberFind.setPwdtrade(null);
        yangMemberFind.setPwd(null);
        return yangMemberFind;

    }

//    @Transactional
//    public void update(YangMember yangMember, YangCurrencyUser yangCurrencyUser) {
//        String lock = yangMember.getMemberId().toString();
//        synchronized (lock.intern())
//        {
//
//             this.memberMapper.updateByPrimaryKeySelective(yangMember);
//             this.yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUser);
//        }
//
//    }


    /**
     *
     * @param memberId
     * @param cyId
     * @param num
     * @param numOptions inc dec
     * @param forzen
     * @param forzenOptions inc dec
     * @return
     */
    public  Integer assets(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4
    ){
        HashMap pama = new HashMap();
        pama.put("memberId",memberId);
        pama.put("cyId",cyId);
        pama.put("num",num);
        pama.put("numOptions",numOptions);
        pama.put("forzen",forzen);
        pama.put("forzenOptions",forzenOptions);


        pama.put("memberId1",memberId1);
        pama.put("cyId1",cyId1);
        pama.put("num1",num1);
        pama.put("numOptions1",numOptions1);
        pama.put("forzen1",forzen1);
        pama.put("forzenOptions1",forzenOptions1);

        pama.put("memberId2",memberId2);
        pama.put("cyId2",cyId2);
        pama.put("num2",num2);
        pama.put("numOptions2",numOptions2);
        pama.put("forzen2",forzen2);
        pama.put("forzenOptions2",forzenOptions2);

        pama.put("memberId3",memberId3);
        pama.put("cyId3",cyId3);
        pama.put("num3",num3);
        pama.put("numOptions3",numOptions3);
        pama.put("forzen3",forzen3);
        pama.put("forzenOptions3",forzenOptions3);

        pama.put("memberId4",memberId4);
        pama.put("cyId4",cyId4);
        pama.put("num4",num4);
        pama.put("numOptions4",numOptions4);
        pama.put("forzen4",forzen4);
        pama.put("forzenOptions4",forzenOptions4);

        return  memberMapper.assets(pama);
    }



}
