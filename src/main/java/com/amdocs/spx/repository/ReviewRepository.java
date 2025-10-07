package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
