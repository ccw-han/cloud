package net.cyweb.message;

public enum CodeMsg {
    ERROR(0,"系统异常"),
    ERROR_OUT_OF_SERVICE(-1,"微服务丢失,断路器生效"),
    SUCCESS(1,"操作成功");

    private int index;
    private String msg;

    CodeMsg(int index, String msg) {
        this.index = index;
        this.msg = msg;
    }

    public int getIndex() {
        return index;
    }

    public String getMsg() {
        return msg;
    }
}
