package com.example.smsforwarder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SmsForwarderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsForwarderApplication.class, args);
    }
}
