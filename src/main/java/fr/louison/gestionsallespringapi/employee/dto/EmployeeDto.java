package fr.louison.gestionsallespringapi.employee.dto;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class EmployeeDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "EmployeeDto_GetEmployeeDto", description = "Récupérer les informations d'une personne")
    public static class GetEmployeeDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private EmployeeRole role;
        private String departmentName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "EmployeeDto_PostInputDto", description = "DTO pour la création d'une personne")
    public static class PostInputDto {
        @NotBlank(message = "Le prénom ne peut pas être vide")
        private String firstName;

        @NotBlank(message = "Le nom ne peut pas être vide")
        private String lastName;

        @NotBlank(message = "L'email ne peut pas être vide")
        @Email(message = "L'email doit avoir un format valide")
        private String email;

        @NotNull(message = "Le rôle est obligatoire")
        private EmployeeRole role;

        @NotBlank(message = "Le mot de passe ne peut pas être vide")
        private String password;

        @NotNull(message = "L'ID du département est obligatoire")
        private Long departmentId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "EmployeeDto_PostOutputDto", description = "DTO pour la réponse après la création d'une personne")
    public static class PostOutputDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private EmployeeRole role;
        private DepartmentEntity department;
    }
}
