package com.zjamss.plainpictureserver;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName: MQUtils
 * @Description: TODO
 * @author: ZJames
 * @date: 2021年09月25日 18:57
 */
@Component("MQUtils")
public class MQUtils {

    //主机
    private static String host;
    //用户名
    private static String username;
    //密码
    private static String password;
    //端口
    private static String port;
    //虚拟机
    private static String vHost;

    @Value("${mq.host}")
    public void setHost(String host) {
        MQUtils.host = host;
    }

    @Value("${mq.username}")
    public void setUsername(String username) {
        MQUtils.username = username;
    }

    @Value("${mq.password}")
    public void setPassword(String password) {
        MQUtils.password = password;
    }

    @Value("${mq.port}")
    public void setPort(String port) {
        MQUtils.port = port;
    }

    @Value("${mq.vHost}")
    public void setvHost(String vHost) {
        MQUtils.vHost = vHost;
    }

    public static Channel getChannel() throws IOException, TimeoutException {
        System.out.println("------------------>host:"+host+"username:"+username);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setPort(5672);
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }

}
