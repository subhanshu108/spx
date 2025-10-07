package com.amdocs.spx.repository;

import com.amdocs.spx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
