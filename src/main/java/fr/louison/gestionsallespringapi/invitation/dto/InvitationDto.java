package fr.louison.gestionsallespringapi.invitation.dto;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class InvitationDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "InvitationDto_PostInputDto", description = "DTO pour la création d'une invitation")
    public static class PostInputDto {

        @NotNull(message = "L'identifiant de la réservation doit être fourni")
        private Long bookingId;

        @NotNull(message = "L'identifiant du participant est obligatoire")
        private Long attendeeId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "InvitationDto_PostOutputDto", description = "DTO pour la réponse après la création d'une invitation")
    public static class PostOutputDto {
        private Long id;
        private InvitationStatus status;
        private BookingSimpleDto booking;
        private EmployeeSimpleDto attendee;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "InvitationDto_GetOutputDto", description = "DTO pour la réponse après la récupération d'une invitation")
    public static class BookingSimpleDto {
        private Long idBooking;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    // DTO court pour l'invité
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "InvitationDto_EmployeeSimpleDto", description = "DTO pour la réponse après la récupération d'un invité")
    public static class EmployeeSimpleDto {
        private Long idEmployee;
        private String firstName;
        private String lastName;
        private String email;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(name = "InvitationDto_PutInputDto", description = "DTO pour la modification du statut d'une invitation")
    public static class PutInputDto {
        @NotNull(message = "Le statut est obligatoire pour la modification")
        private InvitationStatus status;
    }
}
