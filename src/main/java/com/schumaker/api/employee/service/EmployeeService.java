package com.schumaker.api.employee.service;

import com.schumaker.api.employee.exception.EmailAlreadyExistsException;
import com.schumaker.api.employee.model.entity.Employee;
import com.schumaker.api.employee.exception.EmployeeNotFoundException;
import com.schumaker.api.employee.model.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeService {

    private final HobbyService hobbyService;
    private final EmployeeRepository repository;

    @Autowired
    public EmployeeService(HobbyService hobbyService, EmployeeRepository repository) {
        this.hobbyService = hobbyService;
        this.repository = repository;
    }

    public Page<Employee> list(Pageable pagination) {
        return repository.findAll(pagination);
    }

    public Employee getById(UUID id) {
         return repository.findById(id).orElseThrow(EmployeeNotFoundException::new);
    }

    @Transactional
    public Employee create(Employee employee) {
        if (verifyEmail(employee.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        employee.getHobbies().forEach(hobby -> hobby.setEmployee(employee));
        return repository.save(employee);
    }

    @Transactional
    public Employee update(UUID id, Employee employee) {
        var actualEmployee = repository.findById(id).orElseThrow(EmployeeNotFoundException::new);

        if (verifyEmail(employee.getEmail()) && !employee.getEmail().equals(actualEmployee.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        actualEmployee.setEmail(employee.getEmail());
        actualEmployee.setFullName(employee.getFullName());
        actualEmployee.setDateOfBirth(employee.getDateOfBirth());
        actualEmployee.setUpdatedOn(LocalDateTime.now());

        actualEmployee.getHobbies().forEach(hobby -> hobbyService.delete(hobby.getId()));

        actualEmployee.setHobbies(employee.getHobbies());
        actualEmployee.getHobbies().forEach(hobby -> hobby.setEmployee(actualEmployee));

        return repository.save(actualEmployee);
    }

    @Transactional
    public void delete(UUID id) {
       var employee = repository.findById(id).orElseThrow(EmployeeNotFoundException::new);
       repository.delete(employee);
    }

    private boolean verifyEmail(String email) {
        Optional<Employee> employee = repository.findByEmail(email);
        return employee.isPresent();
    }
}
