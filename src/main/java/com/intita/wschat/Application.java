package com.intita.wschat;

import java.util.concurrent.Executor;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.WebSocketTraceChannelInterceptor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextListener;

import com.intita.ws.WebSocketTraceChannelInterceptorAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration
@Import({WebSocketTraceChannelInterceptor.class, WebSocketTraceChannelInterceptorAutoConfiguration.class})
//@ComponentScan("org.springframework.boot.actuate.trace")
public class Application extends SpringBootServletInitializer  implements AsyncConfigurer  {


	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {

		try {
			SpringApplication.run(Application.class, args);	
		} catch (Exception e) {
			
		}

	}

    @Override public void onStartup( ServletContext servletContext ) throws ServletException {
        super.onStartup( servletContext );
        servletContext.addListener( new RequestContextListener() ); 
    }
    
	@Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setThreadNamePrefix("LULExecutor-");
        taskExecutor.initialize();
        return taskExecutor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
    @Value("${ssl.path}")
    String keystoreFile;
    @Value("${ssl.pass}")
    String keystorePass = "qqqqqq";
    final String keystoreType = "PKCS12";
    final String keystoreProvider = "SunJSSE";
    final String keystoreAlias = "tomcat";
    
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
         
        // keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
        // keytool -list -v -keystore keystore.p12 -storetype pkcs12
         
        // curl -u user:password -k https://127.0.0.1:9000/greeting
         
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addConnectorCustomizers((TomcatConnectorCustomizer) (Connector con) -> {
            con.setScheme("https");
            con.setSecure(true);
            Http11NioProtocol proto = (Http11NioProtocol) con.getProtocolHandler();
            proto.setSSLEnabled(true);
            proto.setKeystoreFile(keystoreFile);
            proto.setKeystorePass(keystorePass);
            proto.setKeystoreType(keystoreType);
            proto.setProperty("keystoreProvider", keystoreProvider);
            proto.setKeyAlias(keystoreAlias);
        });
     
         
        return factory;
    }

}
