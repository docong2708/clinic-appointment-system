package com.group01.commonsecurity.config;

import com.group01.commonsecurity.filter.CurrentUserHeaderFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CommonSecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(CurrentUserHeaderFilter.class)
    CurrentUserHeaderFilter currentUserHeaderFilter() {
        return new CurrentUserHeaderFilter();
    }
}
