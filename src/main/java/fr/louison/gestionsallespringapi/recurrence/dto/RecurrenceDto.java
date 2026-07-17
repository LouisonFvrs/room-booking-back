package fr.louison.gestionsallespringapi.recurrence.dto;

import fr.louison.gestionsallespringapi.recurrence.model.enums.RecurrenceType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RecurrenceDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PostInputDto {

        @NotNull(message = "Le type de récurrence est obligatoire")
        private RecurrenceType type;

        @Min(value = 1, message = "La valeur d'interval doit être supérieur à 0")
        private Integer intervalValue;
        
        @NotNull(message = "La date de fin est obligatoire")
        @Future(message = "La date de fin doit être une date future (supérieure à aujourd'hui)")
        private LocalDate endDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PostOutputDto {

        private Long id;
        private RecurrenceType type;
        private Integer intervalValue;
        private LocalDate endDate;
    }
}
