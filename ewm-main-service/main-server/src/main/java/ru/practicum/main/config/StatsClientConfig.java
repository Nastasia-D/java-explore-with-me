package ru.practicum.main.config;

import client.StatsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate statsRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient statsClient(RestTemplate statsRestTemplate, @Value("${stats-server.url}") String statsServerUrl) {
        return new StatsClient(statsRestTemplate, statsServerUrl);
    }
}
