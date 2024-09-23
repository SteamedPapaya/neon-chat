package com.neon.hanasi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles("cluster")
public class RedisClusterTest {


    @Autowired
    private Environment environment;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisClusterConnection() {
        String[] nodes = environment.getProperty("spring.data.redis.cluster.nodes", String[].class);
        System.out.println("Redis Cluster Nodes: " + Arrays.toString(nodes));

        try {
            String key = "testKey";
            String value = "Hello, Redis Cluster!";
            redisTemplate.opsForValue().set(key, value);

            String result = (String) redisTemplate.opsForValue().get(key);
            assertEquals(value, result);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }
    }
}