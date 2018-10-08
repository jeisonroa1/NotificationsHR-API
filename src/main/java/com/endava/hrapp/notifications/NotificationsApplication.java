package com.endava.hrapp.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class NotificationsApplication {

    @PostConstruct
    void started() { TimeZone.setDefault(TimeZone.getTimeZone("GMT-5:00")); }

    public static void main(String[] args) { SpringApplication.run(NotificationsApplication.class, args); }

    @Bean
    public RestTemplate getRestTemplate(RestTemplateBuilder builder) { return builder.build(); }
}
