package fr.louison.gestionsallespringapi.invitation.service;

import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.exception.BadRequestException;
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
        if (invitationDto.getStatus() == InvitationStatus.PENDING) {
            throw new BadRequestException("Une réponse doit être ACCEPTED ou DECLINED.");
        }

        invitation.setStatus(invitationDto.getStatus());

        return mapToPostOutputDto(invitationRepository.save(invitation));
    }

    private InvitationDto.PostOutputDto mapToPostOutputDto(InvitationEntity invitation) {
        return InvitationDto.PostOutputDto.builder()
                .id(invitation.getIdInvitation())
                .status(invitation.getStatus())
                .booking(InvitationDto.BookingSimpleDto.builder()
                        .idBooking(invitation.getBooking().getIdBooking())
                        .startTime(invitation.getBooking().getStartDate())
                        .endTime(invitation.getBooking().getEndDate())
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
