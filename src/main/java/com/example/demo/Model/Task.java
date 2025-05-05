package com.example.demo.Model;

import com.example.demo.Enum.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Task")
public class Task {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "client_inn", referencedColumnName = "inn", nullable = false)
    private Client client;

    @Column(name = "taskStatus")
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Task(String title, String description, Client client){
        this.title = title;
        this.description = description;
        this.client = client;
        this.taskStatus = TaskStatus.NEW;
        this.createdAt = LocalDateTime.now();
    }
}
