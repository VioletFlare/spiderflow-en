package org.spiderflow.core.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Mail Sending Tools
 * 
 * @author BillDowney
 * @date 2020Year4Month4Sun AM12:31:09
 */
@Component
public class EmailUtils {

	// Sendmail service
	@Autowired
	private JavaMailSender javaMailSender;
	// Recipient
	@Value("${spring.mail.username}")
	private String from;

	/**
	 * Send a simple text mail
	 * 
	 * @param subject Topic
	 * @param content 内容
	 * @param to      Address List
	 * @author BillDowney
	 * @date 2020Year4Month4Sun AM12:40:42
	 */
	public void sendSimpleMail(String subject, String content, String... to) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setSubject(subject);
		message.setText(content);
		message.setTo(to);
		javaMailSender.send(message);
	}

}
