package com.example.orchestrator.repo;

// ExecutionRepository.java

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IExecutionRepository extends JpaRepository<Execution, Long> {
}
