package com.example.kafkaadminservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = KafkaAdminServiceApplication.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@ActiveProfiles("test")
class KafkaAdminServiceApplicationTests {

    @MockBean
    private CommandLineRunner commandLineRunner;
    @Test
    void contextLoads() {
    }

}
