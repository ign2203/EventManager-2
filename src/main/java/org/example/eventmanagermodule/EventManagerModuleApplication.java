package org.example.eventmanagermodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventManagerModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagerModuleApplication.class, args);
    }

}
