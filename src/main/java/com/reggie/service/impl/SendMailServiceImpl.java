package com.reggie.service.impl;

import com.reggie.service.SendMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Slf4j
@Service
public class SendMailServiceImpl implements SendMailService {

    @Autowired
    private JavaMailSender javaMailSender;

    //发送人
    private String from = "2418576589@qq.com";
    //接收人
    private String to = "2686582126@qq.com";
    //标题
    private String subject = "测试邮件";
    //正文
    private String text = "测试邮件";

    //点开有惊喜
    private String href = "<img src='https://th.bing.com/th/id/OIP.aLydfoO7Wplq70ZqLkUpDQHaHa?pid=ImgDet&rs=1'/>" +
            "<a href='https://www.bilibili.com/'>点开有惊喜</a>";

    @Override
    public void sendMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from + "(来自aming的邮件)");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    @Override
    public void sendMailMax() {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            //第二个参数，可以添加附件
            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setFrom(from + "(来自aming的邮件)");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(href,true);
            //添加附件
            File file = new File("H:\\img\\9a581a85-90ed-4e9c-92db-71bcb0ac1d7d.jpg");
            helper.addAttachment("一张图片.png",file);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送邮箱验证码
     * @param userEmail
     * @param code
     */
    @Override
    public void sendUserMail(String userEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from + "(来自饿死了么实习小组的邮件)");
        message.setTo(userEmail);
        message.setSubject("来自饿死了么外卖的邮件" +
                "");
        message.setText("您的验证码为：" + code + "。请您妥善保管，不要泄露。");
        javaMailSender.send(message);
    }
}
