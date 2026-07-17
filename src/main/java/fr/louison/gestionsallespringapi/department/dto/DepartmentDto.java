package fr.louison.gestionsallespringapi.department.dto;

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
    public static class PostInputDto {
        @NotNull
        @NotBlank(message = "Un nom doit être fourni")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostOutputDto {
        private Long id;
        @NotNull
        @NotBlank(message = "Un nom doit être fourni")
        private String name;
    }
}
