package com.zjamss.plainpictureserver.controller;

import com.rabbitmq.client.Channel;
import com.zjamss.plainpictureserver.MQUtils;
import com.zjamss.plainpictureserver.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.concurrent.TimeoutException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.content.image.png;

/**
 * @Program: PlainPicture-Server
 * @Description:
 * @Author: ZJamss
 * @Create: 2022-03-07 15:41
 **/
@RestController
@DependsOn("MessageService")
public class ServerController {

    public static final String PATH = "/img";
    public static final String FILE = "/img/cachePic.jpg";
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    private MessageService messageService;

    private static Channel channel;

    @Value("${mq.config.exchange_name}")
    public void setExchangeName(String exchangeName) {
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

    @GetMapping(value = "/getPic", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getPic() throws IOException {
        File dir = new File(PATH);
        dir.setWritable(true, false);    //设置写权限，windows下不用此语句
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(FILE);
        if (!file.exists()) {
            return null;
        }
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[fis.available()];
        fis.read(bytes, 0, fis.available());
        LOGGER.info("拉取了一张图片：" + file.getName());
        return bytes;
    }

    @PostMapping("/uploadPic")
    public void uploadPic(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            file.transferTo(new File(FILE));
            file.transferTo(new File(PATH + file.getOriginalFilename()));
            messageService.sendMessage("New_File");
            LOGGER.info("上传了一张图片：" + file.getOriginalFilename());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
