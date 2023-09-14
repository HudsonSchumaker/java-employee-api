package com.schumaker.api.employee.view.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeForm {

    @Email
    @NotNull
    private String email;

    @NotNull
    @NotBlank
    private String fullName;

    @NotNull
    @Past(message = "Date of birth should be a past date")
    private LocalDate dateOfBirth;

    @Valid
    List<HobbyForm> hobbies = new ArrayList<>();
}
