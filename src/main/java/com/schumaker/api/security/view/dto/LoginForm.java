package com.schumaker.api.security.view.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginForm {

    @NotNull
    private String email;

    @NotNull
    private String password;
}
