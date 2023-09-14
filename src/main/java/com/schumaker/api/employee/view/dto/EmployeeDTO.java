package com.schumaker.api.employee.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private UUID id;
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private List<HobbyDTO> hobbies = new ArrayList<>();
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
