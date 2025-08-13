package com.example.demo.EventListener;

import com.example.demo.Event.DeleteUserEvent;
import com.example.demo.Event.UpdateUserEvent;
import com.example.demo.Service.AccountService;
import com.example.demo.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventListener {

    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public UserEventListener(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

@KafkaListener(topics = "user_updated", groupId = "user-service-group")
    public void handleUpdateUser(UpdateUserEvent event){
        try {
            log.info("Received event: {} for user {}", event.type(), event.userId());

            if ("UPDATE_USER".equals(event.type())) {
                userService.updateUser(event.userId(), event.updateUserDTO());
                log.info("User {} updated successfully", event.userId());
            } else {
                log.warn("Unknown event type: {}", event.type());
            }
        } catch (Exception e) {
            log.error("Failed to process UpdateUserEvent for userId={}", event.userId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "user_deleted", groupId = "user-service-group")
    public void handleDeleteUser(DeleteUserEvent event){
        try {
            log.info("Received event: {} for user {}", event.type(), event.userId());

            if ("DELETE_USER".equals(event.type())) {
                accountService.deleteAccount(event.userId());
                log.info("User {} deleted successfully", event.userId());
            } else {
                log.warn("Unknown event type: {}", event.type());
            }
        } catch (Exception e) {
            log.error("Failed to process DeleteUserEvent for userId={}", event.userId(), e);
            throw e;
        }
    }
}
