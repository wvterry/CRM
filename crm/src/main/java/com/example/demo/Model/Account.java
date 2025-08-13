package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@ToString(exclude = "user")
@EqualsAndHashCode
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column
    private String email;

    @Column
    private String password;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany
    @JoinTable(name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;


    public Account(Long accountId, String email, User user, Set<Role> roles) {
        this.accountId = accountId;
        this.email = email;
        this.user = user;
        this.roles = roles;
    }

    public Account(Long accountId, String email, User user) {
        this.accountId = accountId;
        this.email = email;
        this.user = user;
    }

    public Account(Long accountId, String email, String password) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
    }
}
