package com.group01.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        System.setProperty("io.netty.http.strictLineParsing", "false");
        System.setProperty("io.netty.handler.codec.http.defaultStrictLineParsing", "false");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
