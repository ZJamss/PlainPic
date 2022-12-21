package cn.zjamss.pp.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Program: PlainPicture
 * @Description:
 * @Author: ZJamss
 * @Create: 2022-03-08 10:10
 **/
public class MQUtil {
    //主机
    private static final String host = "192.168.15.100";
    //用户名
    private static final String username = "admin";
    //密码
    private static final String password = "admin";
    //端口
    private static final Integer port = 5672;
    //虚拟机
    private static final String vHost = "/";

    public static Channel getChannel() throws IOException, TimeoutException {
        System.out.println("------------------>host:"+host+"username:"+username);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setPort(port);
        factory.setVirtualHost(vHost);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }

}
