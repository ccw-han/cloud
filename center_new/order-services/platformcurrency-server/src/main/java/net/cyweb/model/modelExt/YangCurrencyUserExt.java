package net.cyweb.model.modelExt;

import net.cyweb.model.YangCurrencyUser;

import javax.persistence.Column;
import javax.persistence.Table;


@Table(name = "yang_currency_user")
public class YangCurrencyUserExt extends YangCurrencyUser {



    private String email;

    @Override
    public String getChongzhiUrl() {
        return chongzhiUrl;
    }

    @Override
    public void setChongzhiUrl(String chongzhiUrl) {
        this.chongzhiUrl = chongzhiUrl;
    }

    @Column(name = "chongzhi_url")
    private String chongzhiUrl;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
