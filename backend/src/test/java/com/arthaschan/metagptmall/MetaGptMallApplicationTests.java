package com.arthaschan.metagptmall;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "rocketmq.name-server=localhost:9876",
    "app.jwt.secret=test-secret-key-for-unit-tests-only-32chars",
    "app.jwt.expiration-ms=3600000"
})
class MetaGptMallApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: Spring context loads without errors
    }
}
