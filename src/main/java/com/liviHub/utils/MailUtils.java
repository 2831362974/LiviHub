package com.liviHub.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MailUtils {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // 发送验证码邮件
    public void sendVerificationCode(String recipientEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("【验证码】账号安全验证");

            // 使用HTML格式提高可读性
            String content = "<html><body>" +
                    "<div style='padding:20px; font-family:Arial, sans-serif;'>" +
                    "<h3 style='color:#333;'>尊敬的用户，你好！</h3>" +
                    "<p style='margin-bottom:15px;'>你的注册验证码为：</p>" +
                    "<div style='padding:10px; background-color:#f5f5f5; border-radius:5px; font-size:24px; font-weight:bold; color:#e94e1b;'>" +
                    code +
                    "</div>" +
                    "<p style='margin-top:15px; color:#666;'>有效期为5分钟，请及时完成验证。</p>" +
                    "</div>" +
                    "</body></html>";

            helper.setText(content, true); // 第二个参数true表示启用HTML格式

            mailSender.send(message);
            System.out.println("验证码邮件已发送至: " + recipientEmail);
        } catch (MessagingException e) {
            // 记录详细异常信息
            System.err.println("发送邮件失败，收件人: " + recipientEmail);
            e.printStackTrace();
            // 可以添加重试逻辑或通知管理员的代码
        }
    }

    // 生成6位纯数字验证码（避免字母混淆）
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        // 生成6位数字
        for (int i = 0; i < 6; i++) {
            // 生成2-9的随机数字（避免0和1）
            code.append(random.nextInt(8) + 2);
        }

        return code.toString();
    }
}