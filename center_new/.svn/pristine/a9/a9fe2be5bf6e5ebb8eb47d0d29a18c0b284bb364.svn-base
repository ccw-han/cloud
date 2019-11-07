package net.cyweb.controller;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class ApiBaseController {

    public static String MQ_ORDER_QUEUE = "mountOrder";

    /*获取字段验证错误信息*/
    public String getStringErrors(BindingResult bindingResult){
        StringBuffer sb = new StringBuffer();
        List<String> errors = Lists.newArrayList();
        for(ObjectError objectError : bindingResult.getAllErrors()){
            errors.add(objectError.getDefaultMessage());
        }
        return StringUtils.join(errors,",");
    }



}
