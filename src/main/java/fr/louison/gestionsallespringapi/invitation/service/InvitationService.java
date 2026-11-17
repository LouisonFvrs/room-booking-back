package fr.louison.gestionsallespringapi.invitation.service;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.model.enums.BookingStatus;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.exception.BadRequestException;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import fr.louison.gestionsallespringapi.exception.RessourceNotFoundException;
import fr.louison.gestionsallespringapi.invitation.dto.InvitationDto;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.invitation.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public Page<InvitationDto.PostOutputDto> getInvitationsByBooking(Pageable pageable, Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new RessourceNotFoundException("Réservation introuvable avec l'ID : " + bookingId);
        }

        return invitationRepository.findByBooking_IdBooking(bookingId, pageable)
                .map(this::mapToPostOutputDto);
    }

    @Transactional(readOnly = true)
    public Page<InvitationDto.PostOutputDto> getInvitationsByAttendee(Pageable pageable, Long attendeeId) {
        return invitationRepository.findByEmployeeAttendee_IdEmployee(attendeeId, pageable)
                .map(this::mapToPostOutputDto);
    }

    public InvitationDto.PostOutputDto updateInvitation(Long id, InvitationDto.PutInputDto invitationDto, Long currentEmployeeId) {
        InvitationEntity invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Invitation introuvable avec l'ID : " + id));

        if (!invitation.getEmployeeAttendee().getIdEmployee().equals(currentEmployeeId)) {
            throw new AccessDeniedException("Seul l'invité peut répondre à son invitation.");
        }
        if (invitation.getBooking().getStatus() == BookingStatus.CANCELLED) {
            throw new RessourceConflictException("La réunion a été annulée, l'invitation n'attend plus de réponse.");
        }
        if (invitationDto.getStatus() == InvitationStatus.PENDING) {
            throw new BadRequestException("Une réponse doit être ACCEPTED ou DECLINED.");
        }

        invitation.setStatus(invitationDto.getStatus());

        return mapToPostOutputDto(invitationRepository.save(invitation));
    }

    private InvitationDto.PostOutputDto mapToPostOutputDto(InvitationEntity invitation) {
        BookingEntity booking = invitation.getBooking();
        EmployeeEntity organizer = booking.getEmployeeOrganizer();

        return InvitationDto.PostOutputDto.builder()
                .id(invitation.getIdInvitation())
                .status(invitation.getStatus())
                .booking(InvitationDto.BookingSimpleDto.builder()
                        .idBooking(booking.getIdBooking())
                        .title(booking.getTitle())
                        .status(booking.getStatus())
                        .startTime(booking.getStartDate())
                        .endTime(booking.getEndDate())
                        .roomName(booking.getRoom() != null ? booking.getRoom().getName() : null)
                        .organizerName(organizer != null ? organizer.getFirstName() + " " + organizer.getLastName() : null)
                        .build())
                .attendee(InvitationDto.EmployeeSimpleDto.builder()
                        .idEmployee(invitation.getEmployeeAttendee().getIdEmployee())
                        .firstName(invitation.getEmployeeAttendee().getFirstName())
                        .lastName(invitation.getEmployeeAttendee().getLastName())
                        .email(invitation.getEmployeeAttendee().getEmail())
                        .build())
                .build();
    }
}
