package com.schumaker.api.employee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schumaker.api.EmployeeHelper;
import com.schumaker.api.employee.exception.EmailAlreadyExistsException;
import com.schumaker.api.employee.exception.EmployeeNotFoundException;
import com.schumaker.api.employee.view.dto.EmployeeForm;
import com.schumaker.api.employee.view.dto.HobbyForm;
import com.schumaker.api.employee.model.entity.Employee;
import com.schumaker.api.employee.model.entity.Hobby;
import com.schumaker.api.employee.model.repository.EmployeeRepository;
import com.schumaker.api.employee.model.repository.HobbyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
public class EmployeeServiceIT {
    @Autowired
    EmployeeService employeeService;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    HobbyService hobbyService;

    @Autowired
    HobbyRepository hobbyRepository;

    @Autowired
    ModelMapper modelMapper;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:15");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.8");

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    @AfterEach
    void cleanUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void shouldGetEmployees() throws Exception {
        List<Employee> employees = EmployeeHelper.getEmployeeList();
        employeeRepository.saveAll(employees);

        Pageable pagination = Pageable.ofSize(10);
        var result = employeeService.list(pagination);

        Assertions.assertEquals(result.getTotalElements(), employees.size());
    }

    @Test
    void shouldCreateEmployee() {
        HobbyForm bjj = new HobbyForm();
        bjj.setName("bjj");

        HobbyForm guitar = new HobbyForm();
        guitar.setName("guitar");

        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("flash@jloa.com");
        employeeForm.setFullName("Barry Allen");
        employeeForm.setDateOfBirth(LocalDate.of(1990,12,12));
        employeeForm.setHobbies(List.of(bjj, guitar));

        List<String> formHobbiesNames = employeeForm.getHobbies().stream()
                .map(HobbyForm::getName).toList();

        var employee = modelMapper.map(employeeForm, Employee.class);
        var result = employeeService.create(employee);

        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(result.getEmail(), employeeForm.getEmail());
        Assertions.assertEquals(result.getFullName(), employeeForm.getFullName());
        Assertions.assertEquals(result.getDateOfBirth(), employeeForm.getDateOfBirth());

        List<String> hobbiesNames = result.getHobbies().stream()
                .map(Hobby::getName)
                .toList();

        for(var formHobbiesName : formHobbiesNames) {
            Assertions.assertTrue(hobbiesNames.contains(formHobbiesName));
        }
    }

    @Test
    void shouldCreateAndUpdateEmployee() {
        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("aquaman@jloa.com");
        employeeForm.setFullName("Orin Arthur Curry");
        employeeForm.setDateOfBirth(LocalDate.of(1950,8,8));

        var employee = modelMapper.map(employeeForm, Employee.class);
        var created = employeeService.create(employee);

        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(created.getEmail(), employeeForm.getEmail());
        Assertions.assertEquals(created.getFullName(), employeeForm.getFullName());
        Assertions.assertEquals(created.getDateOfBirth(), employeeForm.getDateOfBirth());

        EmployeeForm employeeUpdateForm = new EmployeeForm();
        employeeUpdateForm.setEmail("aquaman@jloa.com");
        employeeUpdateForm.setFullName("Arthur Curry");
        employeeUpdateForm.setDateOfBirth(LocalDate.of(1951,8,8));

        var employeeUpdate = modelMapper.map(employeeUpdateForm, Employee.class);
        var updated = employeeService.update(created.getId(), employeeUpdate);

        Assertions.assertEquals(updated.getId(), created.getId());
        Assertions.assertEquals(updated.getEmail(), employeeUpdateForm.getEmail());
        Assertions.assertEquals(updated.getFullName(), employeeUpdateForm.getFullName());
        Assertions.assertEquals(updated.getDateOfBirth(), employeeUpdateForm.getDateOfBirth());
    }

    @Test
    void shouldCreateAndUpdateEmployeeHobbies() {
        HobbyForm bjj = new HobbyForm();
        bjj.setName("bjj");

        HobbyForm guitar = new HobbyForm();
        guitar.setName("guitar");

        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("flash@jloa.com");
        employeeForm.setFullName("Barry Allen");
        employeeForm.setDateOfBirth(LocalDate.of(1990,12,12));
        employeeForm.setHobbies(List.of(bjj, guitar));

        var employee = modelMapper.map(employeeForm, Employee.class);
        var created = employeeService.create(employee);

        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(created.getEmail(), employeeForm.getEmail());
        Assertions.assertEquals(created.getFullName(), employeeForm.getFullName());
        Assertions.assertEquals(created.getDateOfBirth(), employeeForm.getDateOfBirth());

        HobbyForm violin = new HobbyForm();
        violin.setName("violin");

        HobbyForm judo = new HobbyForm();
        judo.setName("judo");

        EmployeeForm employeeUpdateForm = new EmployeeForm();
        employeeUpdateForm.setEmail("aquaman@jloa.com");
        employeeUpdateForm.setFullName("Arthur Curry");
        employeeUpdateForm.setDateOfBirth(LocalDate.of(1951,8,8));
        employeeUpdateForm.setHobbies(List.of(violin, judo));
        List<String> updatedHobbiesNames = employeeUpdateForm.getHobbies().stream()
                .map(HobbyForm::getName).toList();

        var employeeUpdate = modelMapper.map(employeeUpdateForm, Employee.class);
        var updated = employeeService.update(created.getId(), employeeUpdate);
        List<String> savedHobbiesNames = updated.getHobbies().stream()
                .map(Hobby::getName).toList();

        Assertions.assertEquals(updated.getId(), created.getId());
        Assertions.assertEquals(updated.getEmail(), employeeUpdateForm.getEmail());
        Assertions.assertEquals(updated.getFullName(), employeeUpdateForm.getFullName());
        Assertions.assertEquals(updated.getDateOfBirth(), employeeUpdateForm.getDateOfBirth());

        for(var updatedHobbiesName : updatedHobbiesNames) {
            Assertions.assertTrue(savedHobbiesNames.contains(updatedHobbiesName));
        }
    }

    @Test
    void shouldCreateAndUpdateEmployeeRemoveHobbies() {
        HobbyForm bjj = new HobbyForm();
        bjj.setName("bjj");

        HobbyForm guitar = new HobbyForm();
        guitar.setName("guitar");

        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("flash@jloa.com");
        employeeForm.setFullName("Barry Allen");
        employeeForm.setDateOfBirth(LocalDate.of(1990,12,12));
        employeeForm.setHobbies(List.of(bjj, guitar));

        var employee = modelMapper.map(employeeForm, Employee.class);
        var created = employeeService.create(employee);

        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(created.getEmail(), employeeForm.getEmail());
        Assertions.assertEquals(created.getFullName(), employeeForm.getFullName());
        Assertions.assertEquals(created.getDateOfBirth(), employeeForm.getDateOfBirth());

        EmployeeForm employeeUpdateForm = new EmployeeForm();
        employeeUpdateForm.setEmail("aquaman@jloa.com");
        employeeUpdateForm.setFullName("Arthur Curry");
        employeeUpdateForm.setDateOfBirth(LocalDate.of(1951,8,8));

        var employeeUpdate = modelMapper.map(employeeUpdateForm, Employee.class);
        var updated = employeeService.update(created.getId(), employeeUpdate);

        Assertions.assertEquals(updated.getId(), created.getId());
        Assertions.assertEquals(updated.getEmail(), employeeUpdateForm.getEmail());
        Assertions.assertEquals(updated.getFullName(), employeeUpdateForm.getFullName());
        Assertions.assertEquals(updated.getDateOfBirth(), employeeUpdateForm.getDateOfBirth());
        Assertions.assertEquals(updated.getHobbies().size(), 0);
    }

    @Test
    void shouldNotCreateWithExistingEmail() {
        List<Employee> employees = EmployeeHelper.getEmployeeList();
        employeeRepository.saveAll(employees);

        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("superman@jloa.com");
        employeeForm.setFullName("Kal-El");
        employeeForm.setDateOfBirth(LocalDate.of(1900, 10,1));

        var employee = modelMapper.map(employeeForm, Employee.class);
        assertThrows(EmailAlreadyExistsException.class, () -> {
            employeeService.create(employee);
        });
    }

    @Test
    void shouldNotUpdateWithExistingEmail() {
        List<Employee> employees = EmployeeHelper.getEmployeeList();
        employeeRepository.saveAll(employees);

        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("kiptonian@jloa.com");
        employeeForm.setFullName("Kal-El");
        employeeForm.setDateOfBirth(LocalDate.of(1900, 10,1));

        var employee = modelMapper.map(employeeForm, Employee.class);
        var created = employeeService.create(employee);

        EmployeeForm employeeUpdateForm = new EmployeeForm();
        employeeUpdateForm.setEmail("superman@jloa.com");
        employeeUpdateForm.setFullName("Kal-El");
        employeeUpdateForm.setDateOfBirth(LocalDate.of(1900, 10,1));

        var employeeUpdate = modelMapper.map(employeeUpdateForm, Employee.class);
        assertThrows(EmailAlreadyExistsException.class, () -> {
            employeeService.update(created.getId(), employeeUpdate);
        });
    }

    @Test
    void shouldGetById() {
        List<Employee> employees = EmployeeHelper.getEmployeeList();
        var saved = employeeRepository.saveAll(employees);
        var id = saved.get(0).getId();

        var result = employeeService.getById(id);

        Assertions.assertEquals(result.getId(), saved.get(0).getId());
        Assertions.assertEquals(result.getEmail(), saved.get(0).getEmail());
        Assertions.assertEquals(result.getFullName(), saved.get(0).getFullName());
        Assertions.assertEquals(result.getDateOfBirth(), saved.get(0).getDateOfBirth());
    }

    @Test
    void shouldDelete() {
        List<Employee> employees = EmployeeHelper.getEmployeeList();
        var result = employeeRepository.saveAll(employees);
        var id = result.get(0).getId();

        employeeService.delete(id);

        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.getById(id);
        });
    }
}
