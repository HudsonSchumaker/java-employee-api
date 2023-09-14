package com.schumaker.api.employee.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email
    @NotNull
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    private String fullName;

    @NotNull
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Hobby> hobbies = new ArrayList<>();

    private LocalDateTime createdOn = LocalDateTime.now();
    private LocalDateTime updatedOn = LocalDateTime.now();

}
