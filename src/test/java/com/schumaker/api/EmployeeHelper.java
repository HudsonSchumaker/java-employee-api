package com.schumaker.api;

import com.schumaker.api.employee.view.dto.EmployeeDTO;
import com.schumaker.api.employee.view.dto.EmployeeForm;
import com.schumaker.api.employee.view.dto.HobbyDTO;
import com.schumaker.api.employee.model.entity.Employee;
import com.schumaker.api.employee.model.entity.Hobby;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmployeeHelper {

    public static EmployeeDTO mapEmployee2EmployeeDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employee.getId());
        employeeDTO.setEmail(employee.getEmail());
        employeeDTO.setFullName(employee.getFullName());
        employeeDTO.setDateOfBirth(employee.getDateOfBirth());

        List<HobbyDTO> hobbiesDTO = new ArrayList<>();
        for (var hobby : employee.getHobbies()) {
            hobbiesDTO.add(new HobbyDTO(hobby.getId(), hobby.getName()));
        }
        employeeDTO.setHobbies(hobbiesDTO);

        return employeeDTO;
    }

    public static Employee mapEmployeeForm2Employee(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setEmail(employeeForm.getEmail());
        employee.setFullName(employeeForm.getFullName());
        employee.setDateOfBirth(employeeForm.getDateOfBirth());

        List<Hobby> hobbies = new ArrayList<>();
        for (var hobbyForm : employeeForm.getHobbies()) {
            Hobby hobby = new Hobby();
            hobby.setName(hobbyForm.getName());
            hobbies.add(hobby);
        }
        employee.setHobbies(hobbies);

        return employee;
    }

    public static EmployeeForm getEmployeeForm() {
        return new EmployeeForm(
                "batman@jloa.com",
                "Bruce Wayne",
                LocalDate.of(1977, 1,1),
                List.of());
    }

    public static Employee getEmployee() {
        return new Employee(
                UUID.randomUUID(),
                "superman@jloa.com",
                "Clark Kent",
                LocalDate.of(1900, 10,1),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    public static List<Employee> getEmployeeList() {
        Employee employee = new Employee(
                UUID.randomUUID(),
                "batman@jloa.com",
                "Bruce Wayne",
                LocalDate.of(1977, 1,1),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now());

        Employee employee2 = new Employee(
                UUID.randomUUID(),
                "superman@jloa.com",
                "Clark Kent",
                LocalDate.of(1900, 10,1),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now());

        return List.of(employee, employee2);
    }
}
