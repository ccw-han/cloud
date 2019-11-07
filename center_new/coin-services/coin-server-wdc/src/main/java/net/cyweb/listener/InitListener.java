package net.cyweb.listener;

import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinTools.CoinFactory;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.CoinService;
import org.apache.tomcat.util.descriptor.web.ContextHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.ContentHandler;
import java.util.Currency;
import java.util.List;

@WebListener
public class InitListener  implements ServletContextListener {



    private CoinService coinService;

    private  List<YangCurrency> list;

    @Override
    public void contextInitialized(ServletContextEvent sce) {


    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
