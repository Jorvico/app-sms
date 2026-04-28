package com.example.smsforwarder.repository;

import com.example.smsforwarder.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByNameIgnoreCase(String name);
}
