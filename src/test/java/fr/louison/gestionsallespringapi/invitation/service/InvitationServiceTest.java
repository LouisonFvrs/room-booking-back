package fr.louison.gestionsallespringapi.invitation.service;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.model.enums.BookingStatus;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.exception.BadRequestException;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import fr.louison.gestionsallespringapi.invitation.dto.InvitationDto;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.invitation.repository.InvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InvitationServiceTest {

    private static final long INVITATION_ID = 5L;
    private static final long ATTENDEE_ID = 1L;
    private static final long OTHER_EMPLOYEE_ID = 2L;

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private BookingRepository bookingRepository;

    private InvitationService invitationService;
    private BookingEntity booking;

    @BeforeEach
    void setUp() {
        invitationService = new InvitationService(invitationRepository, bookingRepository);

        booking = BookingEntity.builder()
                .idBooking(100L)
                .startDate(LocalDateTime.of(2030, 1, 7, 10, 0))
                .endDate(LocalDateTime.of(2030, 1, 7, 11, 0))
                .status(BookingStatus.CONFIRMED)
                .build();
        InvitationEntity invitation = InvitationEntity.builder()
                .idInvitation(INVITATION_ID)
                .status(InvitationStatus.PENDING)
                .booking(booking)
                .employeeAttendee(EmployeeEntity.builder().idEmployee(ATTENDEE_ID).build())
                .build();

        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(InvitationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void theAttendeeCanAcceptTheInvitation() {
        InvitationDto.PostOutputDto updated =
                invitationService.updateInvitation(INVITATION_ID, answer(InvitationStatus.ACCEPTED), ATTENDEE_ID);

        assertThat(updated.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    @Test
    void anotherEmployeeCannotAnswerTheInvitation() {
        assertThatThrownBy(() ->
                invitationService.updateInvitation(INVITATION_ID, answer(InvitationStatus.ACCEPTED), OTHER_EMPLOYEE_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void pendingIsNotAValidAnswer() {
        assertThatThrownBy(() ->
                invitationService.updateInvitation(INVITATION_ID, answer(InvitationStatus.PENDING), ATTENDEE_ID))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void anInvitationToACancelledBookingCannotBeAnswered() {
        booking.setStatus(BookingStatus.CANCELLED);

        assertThatThrownBy(() ->
                invitationService.updateInvitation(INVITATION_ID, answer(InvitationStatus.ACCEPTED), ATTENDEE_ID))
                .isInstanceOf(RessourceConflictException.class);
    }

    private InvitationDto.PutInputDto answer(InvitationStatus status) {
        return InvitationDto.PutInputDto.builder().status(status).build();
    }
}
