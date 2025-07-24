package com.example.demo.Repository;

import com.example.demo.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByClientInn(Long clientInn);

    @Query("SELECT t FROM Task t WHERE t.author.userId = :userId OR t.assignee.userId = :userId")
    List<Task> findAllByUserId(@Param("userId") Long userid);

    List<Task> findByAuthorUserId(Long userId);
}
