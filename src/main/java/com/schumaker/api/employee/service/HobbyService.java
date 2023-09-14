package com.schumaker.api.employee.service;

import com.schumaker.api.employee.model.repository.HobbyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HobbyService {

    private final HobbyRepository repository;

    @Autowired
    public HobbyService(HobbyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
