package com.bocloud.paas.service.utils;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.bocloud.common.model.Result;

/**
 * 邮件发送器
 * 
 * @author songsong
 *
 */
@Component("emailSender")
public class EmailSender {
	private static Logger logger = LoggerFactory.getLogger(EmailSender.class);
	@Autowired
	private EmailConfig emailConfig;

	public Result send(String subject, String receiver, String mailContent) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();

		sender.setHost(emailConfig.getHost());
		sender.setPort(emailConfig.getPort());
		sender.setUsername(emailConfig.getAccount());
		sender.setPassword(emailConfig.getPassword());

		Properties pro = System.getProperties();
		// SMTP 服务器地址
		pro.setProperty("mail.smtp.auth", "true"); // 需要请求认证
		pro.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		pro.setProperty("mail.smtp.socketFactory.fallback", "false"); 

		sender.setJavaMailProperties(pro);

		MimeMessage message = sender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(emailConfig.getSender()); // 发送人
			helper.setTo(receiver); // 收件人
			helper.setSubject(subject); // 标题
			helper.setText(mailContent); // 内容
			sender.send(message);
			return new Result(true, "send mail success");
		} catch (MessagingException e) {
			logger.error("发送邮件异常", e);
			return new Result(false, "send mail exception");
		} catch (Exception e) {
			logger.error("发送邮件异常", e);
			return new Result(false, "send mail exception");
		}
	}

}