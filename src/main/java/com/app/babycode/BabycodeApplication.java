package com.app.babycode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BabycodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BabycodeApplication.class, args);
    }

}
