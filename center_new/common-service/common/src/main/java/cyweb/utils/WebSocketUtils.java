package cyweb.utils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class WebSocketUtils {
//    private String host = "socket.funcoin.co";
    private String host = "test.socket.funcoin.co";
    private Integer port = 80;

    private static volatile WebSocketUtils sInstance;

//    private WebSocketClient client;

    private WebSocketUtils() {

    }

    public static WebSocketUtils getInstance() {
        if (sInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (sInstance == null) {
                    sInstance = new WebSocketUtils();
                }
            }
        }
        return sInstance;
    }

    public WebSocketClient getClient() throws URISyntaxException {
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
                    System.out.println("收到信息："+new String(bytes.array(),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        };
        return client;
    }
}
