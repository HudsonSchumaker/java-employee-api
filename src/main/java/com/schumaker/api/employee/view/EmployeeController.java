package com.schumaker.api.employee.view;

import com.schumaker.api.employee.view.dto.EmployeeDTO;
import com.schumaker.api.employee.view.dto.EmployeeForm;
import com.schumaker.api.employee.model.entity.Employee;
import com.schumaker.api.employee.model.enumeration.EmployeeEventType;
import com.schumaker.api.employee.service.EmployeeService;
import com.schumaker.api.employee.service.EventPublishService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;
    private final EventPublishService eventPublishService;
    private final ModelMapper modelMapper;
    
    @Autowired
    EmployeeController(EmployeeService service, EventPublishService eventPublishService, ModelMapper modelMapper) {
        this.service = service;
        this.eventPublishService = eventPublishService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public Page<EmployeeDTO> list(@PageableDefault(size = 10) Pageable pagination) {
        var employees = service.list(pagination);
        return employees.map(employee -> modelMapper.map(employee, EmployeeDTO.class));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable @NotNull UUID id) {
        var employee = service.getById(id);
        return ResponseEntity.ok(modelMapper.map(employee, EmployeeDTO.class));
    }

    @PostMapping()
    public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeForm form, UriComponentsBuilder uriBuilder) {
        var employee = modelMapper.map(form, Employee.class);
        employee = service.create(employee);

        var employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
        var address = uriBuilder.path("/employees/{id}").buildAndExpand(employeeDTO.getId()).toUri();

        eventPublishService.publishEvent(EmployeeEventType.CREATED, employeeDTO);
        return ResponseEntity.created(address).body(employeeDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable UUID id, @RequestBody @Valid EmployeeForm form) {
        var employee = modelMapper.map(form, Employee.class);
        employee = service.update(id, employee);
        var employeeDTO = modelMapper.map(employee, EmployeeDTO.class);

        eventPublishService.publishEvent(EmployeeEventType.UPDATED, employeeDTO);
        return ResponseEntity.accepted().body(employeeDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);

        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(id);
        eventPublishService.publishEvent(EmployeeEventType.DELETED, employeeDTO);
        return ResponseEntity.noContent().build();
    }
}
