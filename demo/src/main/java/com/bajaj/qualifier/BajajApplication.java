package com.bajaj.qualifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BajajApplication {

    public static void main(String[] args) {
        SpringApplication.run(BajajApplication.class, args);
    }

    // Define RestTemplate bean here
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}