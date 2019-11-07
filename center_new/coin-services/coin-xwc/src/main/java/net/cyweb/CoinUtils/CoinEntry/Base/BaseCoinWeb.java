package net.cyweb.CoinUtils.CoinEntry.Base;

import net.cyweb.model.Result;

public interface BaseCoinWeb {

    public Result getBalance(String address);

    public Result createUser(String passwd);




}
