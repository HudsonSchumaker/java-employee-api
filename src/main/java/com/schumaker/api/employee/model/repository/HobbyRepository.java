package com.schumaker.api.employee.model.repository;

import com.schumaker.api.employee.model.entity.Hobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface HobbyRepository extends JpaRepository<Hobby, UUID> {}
