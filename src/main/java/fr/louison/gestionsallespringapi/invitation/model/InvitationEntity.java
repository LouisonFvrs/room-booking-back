package fr.louison.gestionsallespringapi.invitation.model;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity(name = "invitation")
public class InvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInvitation;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    @ManyToOne
    @JoinColumn(name = "id_booking",  nullable = false)
    private BookingEntity booking;

    @ManyToOne
    @JoinColumn(name = "id_attendee")
    private EmployeeEntity employeeAttendee;
}
