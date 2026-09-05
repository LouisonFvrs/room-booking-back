package fr.louison.gestionsallespringapi.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DepartmentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "DepartmentDto_PostInputDto", description = "DTO pour la création d'un département")
    public static class PostInputDto {
        @NotNull
        @NotBlank(message = "Un nom doit être fourni")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "DepartmentDto_PostOutputDto", description = "DTO pour la réponse après la création d'un département")
    public static class PostOutputDto {
        private Long id;
        @NotNull
        @NotBlank(message = "Un nom doit être fourni")
        private String name;
    }
}
