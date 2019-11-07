package net.cyweb.controller;



import com.alibaba.fastjson.JSONObject;
import net.cyweb.model.CoinPairRedisBean;
import net.cyweb.model.YangOrders;
import net.cyweb.service.YangOrderService;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "test")
public class testCoinController {

    @Autowired
    YangOrderService yangOrderService;

    /**
     *
     */
    @RequestMapping(value = "testSocket")
    public void test1() throws Exception
    {
        YangOrders yangOrders = new YangOrders();
        yangOrders.setCurrencyTradeId(0);
        yangOrders.setCurrencyId(40);
        yangOrders.setOrdersId(1);
        noticeUser(yangOrders);
    }

    @Async
    public void noticeUser(YangOrders yangOrdersFind ) throws Exception
    {
        String host = "socket.funcoin.co";

        int port = 787;

        WebSocketClient client = new WebSocketClient(new URI("ws://"+host+":"+port),new Draft_17()) {

            @Override
            public void onOpen(ServerHandshake arg0) {
                System.out.println("打开链接");
            }

            @Override
            public void onMessage(String arg0) {
                System.out.println("收到消息"+arg0);
            }

            @Override
            public void onError(Exception arg0) {
                arg0.printStackTrace();
                System.out.println("发生错误已关闭");
            }

            @Override
            public void onClose(int arg0, String arg1, boolean arg2) {
                System.out.println("链接已关闭");
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                try {
                    System.out.println(new String(bytes.array(),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }


        };

        client.connect();
        while(!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)){
            System.out.println("还没有打开");
        }
        System.out.println("打开了");

        //
        Map quers = new HashMap();

        List buy =  yangOrderService.getOrderByfields(yangOrdersFind.getCurrencyId(),yangOrdersFind.getCurrencyTradeId(),yangOrdersFind.getOrdersId(),"buy");
        List sell =  yangOrderService.getOrderByfields(yangOrdersFind.getCurrencyId(),yangOrdersFind.getCurrencyTradeId(),yangOrdersFind.getOrdersId(),"sell");
        CoinPairRedisBean coinPairRedisBean = yangOrderService.getCoinPairRedisBean(yangOrdersFind.getCurrencyId(),yangOrdersFind.getCurrencyTradeId());



        quers.put("cy_id",coinPairRedisBean.getCy_id());
        quers.put("type",2);
        quers.put("buy",buy);
        quers.put("sell",sell);
        quers.put("trade",2);

        client.send(JSONObject.toJSON(quers).toString().getBytes("utf-8"));

        client.close();

    }




}
