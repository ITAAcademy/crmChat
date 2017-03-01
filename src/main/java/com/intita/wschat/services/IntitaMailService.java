package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
@Component
public class IntitaMailService {
	private JavaMailSenderImpl mailSender;
	
	@Autowired
	private VelocityEngine velocityEngine;
	
	@Autowired
	private UserMessageService userMessageService;
	
	 @Value("${chat.mail.username}")
	 String mail_userName;
	 @Value("${chat.mail.password}")
	 String mail_password;
	 @Value("${chat.mail.host}")
     String mail_host;
	 @Value("${chat.mail.port}")
	 int mail_port;
	 @Value("${chat.mail.protocol}")
	 String mail_protocol;
	
	
	@PostConstruct
	public void initMailSender(){
		
		mailSender = new JavaMailSenderImpl();
		mailSender.setProtocol(mail_protocol);
		mailSender.setHost(mail_host);
		mailSender.setPort(mail_port);
		mailSender.setUsername(mail_userName);
		mailSender.setPassword(mail_password);
		Properties mailProps = new Properties();
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
       // mailProps.put("mail.smtp.starttls.required", "true");
       // mailProps.setProperty("mail.smtp.user", userName);
        //mailProps.setProperty("mail.smtp.password", password);
        mailProps.put("mail.smtp.debug", "true");

        mailSender.setJavaMailProperties(mailProps);
		
	}
	public void sendUnreadedMessageToIntitaUser(User to){
		Map<String,List<ChatMessage>> unreadedRoomMessages = userMessageService.getAllUnreadedMessages(to.getChatUser());
		mailSender.send(createPreparatorMessages(to,unreadedRoomMessages));
	}
	
	private MimeMessagePreparator createPreparatorMessages(User to,Map<String,List<ChatMessage>> roomMessages){
		return  new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setTo(to.getEmail());//chatuser.getNickName());//destination email
                message.setFrom("zigzag2341@gmail.com"); // could be parameterized...
                Map model = new HashMap();
                model.put("user", to.getEmail());
                model.put("roomMessages", roomMessages);
                String text = VelocityEngineUtils.mergeTemplateIntoString(
                        velocityEngine, "consultation_email.vm","UTF-8", model);
                message.setText(text, true);
            }
        };
	}
	
	
}
