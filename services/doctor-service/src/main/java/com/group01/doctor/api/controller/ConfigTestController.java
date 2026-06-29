package com.group01.doctor.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor/config")
@RefreshScope
public class ConfigTestController {

    @Value("${app.test-message:Hello from Doctor Service default config}")
    private String testMessage;

    @Value("${spring.datasource.url:No DB url configured}")
    private String dbUrl;

    @Value("${server.port:Unknown port}")
    private String serverPort;

    @GetMapping("/test")
    public Map<String, String> getConfigInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("testMessage", testMessage);
        info.put("dbUrl", dbUrl);
        info.put("serverPort", serverPort);
        return info;
    }
}
