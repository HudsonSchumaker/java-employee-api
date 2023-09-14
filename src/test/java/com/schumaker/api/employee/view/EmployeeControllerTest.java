package com.schumaker.api.employee.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schumaker.api.EmployeeHelper;
import com.schumaker.api.config.SecurityConfig;
import com.schumaker.api.employee.exception.EmployeeNotFoundException;
import com.schumaker.api.employee.view.dto.EmployeeDTO;
import com.schumaker.api.employee.view.dto.EmployeeForm;
import com.schumaker.api.employee.model.entity.Employee;
import com.schumaker.api.employee.model.enumeration.EmployeeEventType;
import com.schumaker.api.security.model.repository.AuthUserRepository;
import com.schumaker.api.security.service.TokenService;
import com.schumaker.api.employee.service.EmployeeService;
import com.schumaker.api.employee.service.EventPublishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import({ SecurityConfig.class })
public class EmployeeControllerTest {
    @MockBean
    EmployeeService service;

    @MockBean
    ModelMapper modelMapper;

    @MockBean
    EventPublishService eventPublishService;

    @MockBean
    TokenService tokenService;

    @MockBean
    AuthUserRepository authUserRepository;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper;

    String token;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    @Test
    void listTest() throws Exception {
        Pageable pagination = Pageable.ofSize(10);
        List<Employee> list  = EmployeeHelper.getEmployeeList();
        Page<Employee> employees = new PageImpl<>(list);

        when(service.list(pagination)).thenReturn(employees);
        when(modelMapper.map(list.get(0), EmployeeDTO.class)).thenReturn(EmployeeHelper.mapEmployee2EmployeeDTO(list.get(0)));
        when(modelMapper.map(list.get(1), EmployeeDTO.class)).thenReturn(EmployeeHelper.mapEmployee2EmployeeDTO(list.get(1)));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName", is(list.get(0).getFullName())))
                .andExpect(jsonPath("$.content[0].email", is(list.get(0).getEmail())))
                .andExpect(jsonPath("$.content[1].fullName", is(list.get(1).getFullName())))
                .andExpect(jsonPath("$.content[1].email", is(list.get(1).getEmail())))
                .andDo(print());

        verify(service).list(pagination);
    }

    @Test
    void getByIdTest() throws Exception {
        Employee employee = EmployeeHelper.getEmployee();
        EmployeeDTO employeeDTO = EmployeeHelper.mapEmployee2EmployeeDTO(employee);

        when(service.getById(employee.getId())).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);

        mockMvc.perform(get("/employees/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(employee.getFullName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())))
                .andDo(print());

        verify(service).getById(employee.getId());
    }

    @Test
    @WithMockUser("spring")
    void createTest() throws Exception {
        EmployeeForm form = EmployeeHelper.getEmployeeForm();
        Employee employee = EmployeeHelper.mapEmployeeForm2Employee(form);
        EmployeeDTO employeeDTO = EmployeeHelper.mapEmployee2EmployeeDTO(employee);

        when(modelMapper.map(form, Employee.class)).thenReturn(employee);
        when(service.create(employee)).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);

        String json = objectMapper.writeValueAsString(form);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName", is(form.getFullName())))
                .andExpect(jsonPath("$.email", is(form.getEmail())))
                .andDo(print());

        verify(service).create(employee);
        verify(eventPublishService).publishEvent(EmployeeEventType.CREATED, employeeDTO);
    }

    @Test
    @WithMockUser("spring")
    void updateTest() throws Exception {
        EmployeeForm form = EmployeeHelper.getEmployeeForm();
        form.setDateOfBirth(LocalDate.of(1966, 1, 1));
        Employee employee = EmployeeHelper.mapEmployeeForm2Employee(form);
        EmployeeDTO employeeDTO = EmployeeHelper.mapEmployee2EmployeeDTO(employee);

        when(modelMapper.map(form, Employee.class)).thenReturn(employee);
        when(service.update(employee.getId(), employee)).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);
        String json = objectMapper.writeValueAsString(form);

        mockMvc.perform(put("/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.dateOfBirth", is("1966-01-01")))
                .andDo(print());

        verify(service).update(employee.getId(), employee);
        verify(eventPublishService).publishEvent(EmployeeEventType.UPDATED, employeeDTO);
    }

    @Test
    @WithMockUser("spring")
    void deleteTest() throws Exception {
        Employee employee = EmployeeHelper.getEmployee();
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employee.getId());

        when(service.update(employee.getId(), employee)).thenReturn(employee);

        mockMvc.perform(delete("/employees/{id}", employee.getId()))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(service).delete(employee.getId());
        verify(eventPublishService).publishEvent(EmployeeEventType.DELETED, employeeDTO);
    }

    @Test
    void getByIdNotFoundTest() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new EmployeeNotFoundException());

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(service).getById(id);
    }

    @Test
    @WithMockUser("spring")
    void createTestWithInvalidForm() throws Exception {
        EmployeeForm form = EmployeeHelper.getEmployeeForm();
        form.setFullName("");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("fullName")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")))
                .andDo(print());
    }

    @Test
    @WithMockUser("spring")
    void updateTestWithInvalidId() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeForm form = EmployeeHelper.getEmployeeForm();
        Employee employee = EmployeeHelper.mapEmployeeForm2Employee(form);

        when(modelMapper.map(form, Employee.class)).thenReturn(employee);
        when(service.update(id, employee)).thenThrow(new EmployeeNotFoundException());

        mockMvc.perform(put("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}