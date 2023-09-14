package com.schumaker.api.employee.view.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HobbyForm {

    @NotNull
    @NotBlank
    private String name;
}
