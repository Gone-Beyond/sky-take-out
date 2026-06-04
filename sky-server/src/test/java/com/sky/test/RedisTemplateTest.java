package com.sky.test;

import com.sky.SkyApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = SkyApplication.class)
public class RedisTemplateTest {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisTemplateTest(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Test
    public void testStringOperations() {
        String key = "test:redis:name";

        redisTemplate.opsForValue().set(key, "sky");
        Object value = redisTemplate.opsForValue().get(key);

        assertEquals("sky", value);

        redisTemplate.delete(key);
        assertNull(redisTemplate.opsForValue().get(key));
    }
}
