package com.intita.wschat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Created by roma on 09.05.17.
 */
@Configuration
public class SsoFilterConfiguration  {
    @Autowired
    OAuth2ClientContext oauth2ClientContext;

    @Bean
    public GenericFilterBean ssoFilter() {
        OAuth2ClientAuthenticationProcessingFilter chatFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/chat");
        OAuth2RestTemplate chatTemplate = new OAuth2RestTemplate(crmChat(), oauth2ClientContext);
        chatFilter.setRestTemplate(chatTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(crmChatResource().getUserInfoUri(), crmChat().getClientId());
        tokenServices.setRestTemplate(chatTemplate);
        chatFilter.setTokenServices(tokenServices);
        return chatFilter;
    }

    @Bean
    @ConfigurationProperties("crmchat.resource")
    public ResourceServerProperties crmChatResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("crmchat.client")
    public AuthorizationCodeResourceDetails crmChat() {
        return new AuthorizationCodeResourceDetails();
    }


}
