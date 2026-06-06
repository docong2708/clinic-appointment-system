package com.group01.commonsecurity.config;

import com.group01.commonsecurity.filter.CurrentUserHeaderFilter;
import com.group01.commonsecurity.jwt.JwtProperties;
import com.group01.commonsecurity.jwt.JwtTokenValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class CommonSecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    JwtTokenValidator jwtTokenValidator(JwtProperties jwtProperties) {
        return new JwtTokenValidator(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(CurrentUserHeaderFilter.class)
    CurrentUserHeaderFilter currentUserHeaderFilter() {
        return new CurrentUserHeaderFilter();
    }
}