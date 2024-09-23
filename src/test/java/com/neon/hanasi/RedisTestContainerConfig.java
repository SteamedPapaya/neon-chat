package com.neon.hanasi;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public class RedisTestContainerConfig {

    public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2.1")
            .withExposedPorts(6379);

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            redisContainer.start();

            String redisHost = redisContainer.getHost();
            Integer redisPort = redisContainer.getFirstMappedPort();

            TestPropertyValues.of(
                    "spring.data.redis.host=" + redisHost,
                    "spring.data.redis.port=" + redisPort
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}