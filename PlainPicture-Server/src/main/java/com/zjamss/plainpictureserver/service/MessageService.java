package com.zjamss.plainpictureserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.MessageProperties;
import com.zjamss.plainpictureserver.MQUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName: MessageService
 * @Description: RabbitMQ消息发送服务类
 * @author: ZJames
 * @date: 2021年09月25日 19:21
 */
@Service("MessageService")
@DependsOn("MQUtils")
public class MessageService {

    private static String EXCHANGE_NAME;

    private static Channel channel;

    @Value("${mq.config.exchange_name}")
    public void setExchangeName(String exchangeName) {
        EXCHANGE_NAME = exchangeName;
    }

    static {
        try {
            channel = MQUtils.getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        //开启发布确认
        channel.confirmSelect();

        //声明交换机并设置发布订阅模式 开启持久化
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);

        ConfirmCallback askCallback = (deliveryTag, multiple) -> {
//            System.out.println(LocalDateTime.now().toString()+"-确认消息"+deliveryTag);
        };
        //失败回调
        ConfirmCallback nackCallback = (deliveryTag, multiple) -> {
            System.out.println("未确认的消息" + deliveryTag);
        };
        channel.addConfirmListener(askCallback, nackCallback);


        //发送消息并开启消息持久化
        channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));

    }
}
