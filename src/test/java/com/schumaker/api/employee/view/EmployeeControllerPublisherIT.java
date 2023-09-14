package com.schumaker.api.employee.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schumaker.api.EmployeeHelper;
import com.schumaker.api.employee.view.dto.EmployeeEvent;
import com.schumaker.api.employee.view.dto.EmployeeForm;
import com.schumaker.api.employee.view.dto.HobbyForm;
import com.schumaker.api.employee.model.enumeration.EmployeeEventType;
import com.schumaker.api.employee.model.repository.EmployeeRepository;
import com.schumaker.api.employee.model.repository.HobbyRepository;
import com.schumaker.api.security.view.dto.LoginForm;
import com.schumaker.api.security.view.dto.TokenDTO;
import com.schumaker.api.employee.service.EmployeeService;
import com.schumaker.api.employee.service.EventPublishService;
import com.schumaker.api.employee.service.HobbyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerPublisherIT {
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
    RabbitTemplate rabbitTemplate;

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
    void shouldSendCreatedEvent() throws Exception {
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

        Message message = rabbitTemplate.receive("employee.queue",100);
        Assertions.assertNotNull(message);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        EmployeeEvent eventReceived = (EmployeeEvent) converter.fromMessage(message);

        Assertions.assertEquals(eventReceived.getType(), EmployeeEventType.CREATED);
        Assertions.assertEquals(eventReceived.getEmployeeDTO().getEmail(), employeeForm.getEmail());
        Assertions.assertEquals(eventReceived.getEmployeeDTO().getFullName(), employeeForm.getFullName());
    }

    @Test
    void shouldSendUpdatedEvent() throws Exception {
        var employee = employeeRepository.save(EmployeeHelper.getEmployee());
        var id = employee.getId();

        EmployeeForm employeeUpdateForm = new EmployeeForm();
        employeeUpdateForm.setEmail("superman@jloa.com");
        employeeUpdateForm.setFullName("Kal-El");
        employeeUpdateForm.setDateOfBirth(LocalDate.of(1900, 10,1));

        String json = objectMapper.writeValueAsString(employeeUpdateForm);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);
        mockMvc.perform(put("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders)
                        .content(json))
                        .andExpect(status().isAccepted())
                        .andExpect(jsonPath("$.fullName", is(employeeUpdateForm.getFullName())))
                        .andExpect(jsonPath("$.email", is(employeeUpdateForm.getEmail())))
                        .andExpect(jsonPath("$.dateOfBirth", is(employeeUpdateForm.getDateOfBirth().toString())))
                        .andDo(print());

        Message message = rabbitTemplate.receive("employee.queue",100);
        Assertions.assertNotNull(message);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        EmployeeEvent eventReceived = (EmployeeEvent) converter.fromMessage(message);

        Assertions.assertEquals(eventReceived.getType(), EmployeeEventType.UPDATED);
        Assertions.assertEquals(eventReceived.getEmployeeDTO().getId(), id);
        Assertions.assertEquals(eventReceived.getEmployeeDTO().getEmail(), employeeUpdateForm.getEmail());
        Assertions.assertEquals(eventReceived.getEmployeeDTO().getFullName(), employeeUpdateForm.getFullName());
    }

    @Test
    void shouldSendDeletedEvent() throws Exception {
        var employee = employeeRepository.save(EmployeeHelper.getEmployee());
        var id = employee.getId();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);
        mockMvc.perform(delete("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders))
                        .andExpect(status().isNoContent())
                        .andDo(print());

        Message message = rabbitTemplate.receive("employee.queue",100);
        Assertions.assertNotNull(message);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        EmployeeEvent eventReceived = (EmployeeEvent) converter.fromMessage(message);

        Assertions.assertEquals(eventReceived.getType(), EmployeeEventType.DELETED);
        Assertions.assertEquals(eventReceived.getEmployeeDTO().getId(), id);
    }
}
