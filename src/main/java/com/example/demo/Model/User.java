package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_detail")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.DETACH, orphanRemoval = false)
    private List<Client> clients = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.DETACH, orphanRemoval = false)
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.DETACH, orphanRemoval = false) //проверить cascade = CascadeType.DETACH, orphanRemoval = false
    private List<Task> createdTask;
    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
}

