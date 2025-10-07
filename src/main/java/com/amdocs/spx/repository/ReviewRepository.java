package com.amdocs.spx.repository;

import com.amdocs.spx.Entity.Review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
