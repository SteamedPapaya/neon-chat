package com.neon.hanasi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = RedisTestContainerConfig.Initializer.class)
class HanasiApplicationTests {

    @Test
    void contextLoads() {
    }
}