package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_detail")
public class User {

    @Id
    private Long userId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.DETACH, orphanRemoval = false)
    private List<Client> clients = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.DETACH, orphanRemoval = false)
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.DETACH, orphanRemoval = false)
    private List<Task> createdTask;


    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(Long userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(Long userId) {
        this.userId = userId;
    }
}

