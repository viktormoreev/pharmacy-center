package com.inf.cscb869_pharmacy.data.repo;

import com.inf.cscb869_pharmacy.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
