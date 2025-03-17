package com.birp.chat_backend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.birp.chat_backend.security.AuthentificatedUserFilter;
import com.birp.chat_backend.security.OtpAuthentificationFilter;
import com.birp.chat_backend.services.SessionService;
import com.birp.chat_backend.services.UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final SessionService sessionService;
    private final UserService userService;
    
    @Bean
    public FilterRegistrationBean<OtpAuthentificationFilter> otpFilterRegistration() {
        FilterRegistrationBean<OtpAuthentificationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new OtpAuthentificationFilter(sessionService));
        registrationBean.addUrlPatterns("/api/v1/login", "/api/v1/register");
        registrationBean.setOrder(1);
        return registrationBean;
    }
    
    @Bean
    public FilterRegistrationBean<AuthentificatedUserFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthentificatedUserFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthentificatedUserFilter(sessionService, userService));
        registrationBean.addUrlPatterns("/api/v1/users");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}