package com.intita.wschat.services;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import com.intita.wschat.dto.model.UserMessageDTO;
import com.intita.wschat.web.CommonController;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Component
public class IntitaMailService {
	private JavaMailSenderImpl mailSender;
	
	@Autowired
	private VelocityEngine velocityEngine;
	
	@Autowired private UserMessageService userMessageService;
	@Autowired private ConfigParamService configParamService;

	private final static Logger log = LoggerFactory.getLogger(CommonController.class);
	
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
	 @Value("${chat.mail.smtp.auth}")
	 String mail_smtp_auth;
	 @Value("${chat.mail.smtp.starttls.enable}")
	 String mail_smtp_starttls_enable;
	
	
	@PostConstruct
	public void initMailSender(){
		
		mailSender = new JavaMailSenderImpl();
		mailSender.setProtocol(mail_protocol);
		mailSender.setHost(mail_host);
		mailSender.setPort(mail_port);
		mailSender.setUsername(mail_userName);
		mailSender.setPassword(mail_password);
		Properties mailProps = new Properties();
        mailProps.put("mail.smtp.auth", mail_smtp_auth);
        mailProps.put("mail.smtp.starttls.enable", mail_smtp_starttls_enable);
       // mailProps.put("mail.smtp.starttls.required", "true");
       // mailProps.setProperty("mail.smtp.user", userName);
        //mailProps.setProperty("mail.smtp.password", password);
        mailProps.put("mail.smtp.debug", "true");

        mailSender.setJavaMailProperties(mailProps);
		
	}
	public void sendUnreadedMessageToIntitaUserFrom24Hours(User to) throws Exception{
		Map<Room, List<UserMessageDTO>> unreadedRoomMessages = userMessageService.getAllUnreadedMessagesFrom24Hours(to.getChatUser());
		log.info(to.getEmail()+"  messages_count: "+unreadedRoomMessages.size());
		if(unreadedRoomMessages.size() == 0)
			return;
		mailSender.send(createPreparatorMessages(to,unreadedRoomMessages));
	}
	
	private MimeMessagePreparator createPreparatorMessages(User to, Map<Room, List<UserMessageDTO>> roomMessages) {
		return  new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setTo(to.getEmail());//chatuser.getNickName());//destination email
                //message.setTo("nico13051995@gmail.com");//chatuser.getNickName());//destination email
                message.setFrom(configParamService.getParam("newsletterMail").getValue()); // could be parameterized...
 
    			
                Map<String, Object> model = new HashMap();
                model.put("user", to);
                ConfigParam baseUrl =  configParamService.getParam("fullChatPath");
                model.put("baseUrl", baseUrl.getValue());
                model.put("roomMessages", roomMessages);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
				model.put("dateFormat",dateFormat);
                String text = VelocityEngineUtils.mergeTemplateIntoString(
						velocityEngine, "consultation_email.vm","UTF-8", model);
                mimeMessage.setContent(text, "text/html; charset=utf-8");
                mimeMessage.setSubject("Не прочитані повідомлення!!!","UTF-8");
                //message.setText(text, true);
            }
        };
	}
	
	
}
