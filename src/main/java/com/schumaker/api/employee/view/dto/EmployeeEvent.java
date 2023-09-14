package com.schumaker.api.employee.view.dto;

import com.schumaker.api.employee.model.enumeration.EmployeeEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEvent implements Serializable {

    private EmployeeEventType type;
    private EmployeeDTO employeeDTO;
    private LocalDateTime publishedTime = LocalDateTime.now();

    public EmployeeEvent(EmployeeEventType type,EmployeeDTO employeeDTO) {
        this.type = type;
        this.employeeDTO = employeeDTO;
    }
}
