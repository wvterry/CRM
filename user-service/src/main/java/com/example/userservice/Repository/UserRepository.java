package com.example.userservice.Repository;

import com.example.userservice.Model.User;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRepository extends ListCrudRepository<User, Long> {
}
