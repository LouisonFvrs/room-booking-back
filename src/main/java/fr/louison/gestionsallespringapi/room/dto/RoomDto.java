package fr.louison.gestionsallespringapi.room.dto;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class RoomDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "RoomDto_SimpleDto", description = "DTO représentant une salle de réunion")
    public static class SimpleDto {
        private Long idRoom;
        private String name;
        private int capacity;
        private boolean isAvailable;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "RoomDto_PostOutputDto", description = "DTO représentant une salle de réunion avec ses équipements")
    public static class PostOutputDto {
        private Long idRoom;
        private String name;
        private int capacity;
        private boolean isAvailable;
        private List<EquipmentDto.SimpleDto> equipments;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "RoomDto_PostInputDto", description = "DTO pour la création d'une salle de réunion")
    public static class PostInputDto {

        @NotBlank(message = "Le nom de la salle ne peut pas être vide")
        private String name;

        @Min(value = 5, message = "La capacité minimale est de 5 personnes")
        private int capacity;

        private List<Long> equipmentIds;

    }
}
