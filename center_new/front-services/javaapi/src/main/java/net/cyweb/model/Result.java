package net.cyweb.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * Created by wuhongbing on 2017/12/1.
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class Result {
    private Integer code;
    private Integer errorCode;
    private String msg;
    private Object data;

    public static class Code{
        public static final Integer  SUCCESS = 1;
        public static final Integer ERROR = 0;
    }
}
