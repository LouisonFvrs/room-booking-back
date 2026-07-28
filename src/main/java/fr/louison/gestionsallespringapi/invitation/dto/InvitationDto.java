package fr.louison.gestionsallespringapi.invitation.dto;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
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
    public static class PutInputDto {
        @NotNull(message = "Le statut est obligatoire pour la modification")
        private InvitationStatus status;
    }
}
