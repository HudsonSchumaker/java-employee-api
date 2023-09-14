package com.schumaker.api.employee.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schumaker.api.EmployeeHelper;
import com.schumaker.api.employee.view.dto.EmployeeDTO;
import com.schumaker.api.employee.view.dto.EmployeeForm;
import com.schumaker.api.employee.view.dto.HobbyForm;
import com.schumaker.api.employee.model.entity.Employee;
import com.schumaker.api.employee.model.repository.EmployeeRepository;
import com.schumaker.api.employee.model.repository.HobbyRepository;
import com.schumaker.api.security.view.dto.LoginForm;
import com.schumaker.api.security.view.dto.TokenDTO;
import com.schumaker.api.employee.service.EmployeeService;
import com.schumaker.api.employee.service.EventPublishService;
import com.schumaker.api.employee.service.HobbyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerIT {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    HobbyService hobbyService;

    @Autowired
    HobbyRepository hobbyRepository;

    @Autowired
    EventPublishService eventPublishService;

    @Autowired
    MockMvc mockMvc;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:15");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.8");

    ObjectMapper objectMapper;

    String token;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        try {
            var login = new LoginForm("alfred@jloa.com", "123456");
            String json = objectMapper.writeValueAsString(login);
            var result = this.mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                            .andDo(print())
                            .andReturn();

            var tokenDTO = objectMapper.readValue(result.getResponse().getContentAsString(), TokenDTO.class);
            token = "Bearer " + tokenDTO.getToken();
        } catch (Exception ignore) {}
    }

    @AfterEach
    void cleanUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void shouldGetEmployees() throws Exception {
        List<Employee> employees = EmployeeHelper.getEmployeeList();
        employeeRepository.saveAll(employees);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName", is(employees.get(0).getFullName())))
                .andExpect(jsonPath("$.content[0].email", is(employees.get(0).getEmail())))
                .andExpect(jsonPath("$.content[1].fullName", is(employees.get(1).getFullName())))
                .andExpect(jsonPath("$.content[1].email", is(employees.get(1).getEmail())))
                .andDo(print());

    }

    @Test
    void shouldCreateEmployee() throws Exception {
        HobbyForm bjj = new HobbyForm();
        bjj.setName("bjj");

        HobbyForm guitar = new HobbyForm();
        guitar.setName("guitar");

        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("flash@jloa.com");
        employeeForm.setFullName("Barry Allen");
        employeeForm.setDateOfBirth(LocalDate.of(1990,12,12));
        employeeForm.setHobbies(List.of(bjj, guitar));

        String json = objectMapper.writeValueAsString(employeeForm);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.fullName", is(employeeForm.getFullName())))
                        .andExpect(jsonPath("$.email", is(employeeForm.getEmail())))
                        .andExpect(jsonPath("$.hobbies", hasSize(employeeForm.getHobbies().size())))
                        .andDo(print());
    }

    @Test
    void shouldCreateAndUpdateEmployee() throws Exception {
        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("aquaman@jloa.com");
        employeeForm.setFullName("Orin Arthur Curry");
        employeeForm.setDateOfBirth(LocalDate.of(1950,8,8));

        String json = objectMapper.writeValueAsString(employeeForm);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);
        var result = mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.fullName", is(employeeForm.getFullName())))
                        .andExpect(jsonPath("$.email", is(employeeForm.getEmail())))
                        .andDo(print())
                        .andReturn();

        EmployeeDTO createdEmployee = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeDTO.class);
        var id = createdEmployee.getId();

        EmployeeForm employeeUpdateForm = new EmployeeForm();
        employeeUpdateForm.setEmail("aquaman@jloa.com");
        employeeUpdateForm.setFullName("Arthur Curry");
        employeeUpdateForm.setDateOfBirth(LocalDate.of(1951,8,8));

        json = objectMapper.writeValueAsString(employeeUpdateForm);
        mockMvc.perform(put("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders)
                        .content(json))
                        .andExpect(status().isAccepted())
                        .andExpect(jsonPath("$.fullName", is(employeeUpdateForm.getFullName())))
                        .andExpect(jsonPath("$.email", is(employeeUpdateForm.getEmail())))
                        .andExpect(jsonPath("$.dateOfBirth", is(employeeUpdateForm.getDateOfBirth().toString())))
                        .andDo(print());
    }

    @Test
    void shouldCreateAndDeleteEmployee() throws Exception {
        EmployeeForm employeeForm = new EmployeeForm();
        employeeForm.setEmail("ice@jloa.com");
        employeeForm.setFullName("Tora Olafsdotter");
        employeeForm.setDateOfBirth(LocalDate.of(1958,7,7));

        String json = objectMapper.writeValueAsString(employeeForm);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);
        var result = mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.fullName", is(employeeForm.getFullName())))
                        .andExpect(jsonPath("$.email", is(employeeForm.getEmail())))
                        .andDo(print())
                        .andReturn();

        EmployeeDTO createdEmployee = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeDTO.class);
        var id = createdEmployee.getId();

        mockMvc.perform(delete("/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andExpect(status().isNoContent())
                .andDo(print());

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
