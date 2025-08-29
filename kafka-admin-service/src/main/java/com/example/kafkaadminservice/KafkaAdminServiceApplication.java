package com.example.kafkaadminservice;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class KafkaAdminServiceApplication implements CommandLineRunner {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public static void main(String[] args) {
        SpringApplication.run(KafkaAdminServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(properties)) {
            List<NewTopic> topics = List.of(
                    new NewTopic("user_updated", 1, (short) 1),
                    new NewTopic("user_deleted", 1, (short) 1)
            );

            CreateTopicsResult result = adminClient.createTopics(topics);

            try {
                result.all().get();
                System.out.println("Топики созданы или уже существуют");
            } catch (ExecutionException e) {
                if (e.getCause() instanceof TopicExistsException) {
                    System.out.println("Топики уже существуют");
                } else {
                    throw e;
                }
            }
        }
        System.exit(0);
    }

}
