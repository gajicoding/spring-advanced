package org.example.expert.domain.manager.repository;

import org.example.expert.domain.manager.entity.Manager;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<Manager> findByTodoId(Long todoId);
}
