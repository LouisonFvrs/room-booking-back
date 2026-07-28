package fr.louison.gestionsallespringapi.invitation.service;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.exception.RessourceNotFoundException;
import fr.louison.gestionsallespringapi.invitation.dto.InvitationDto;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.invitation.repository.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Page<InvitationDto.PostOutputDto> getInvitationsByBooking(Pageable pageable, Long bookingId) {

        if (!bookingRepository.existsById(bookingId)) {
            throw new RessourceNotFoundException("Réservation introuvable avec l'ID : " + bookingId);
        }

        Page<InvitationEntity> invitations = invitationRepository.findByBooking(pageable, bookingId);

        return invitations.map(this::mapToPostOutputDto);
    }

    public InvitationDto.PostOutputDto updateInvitation(Long id, InvitationDto.PutInputDto invitationDto) {

        InvitationEntity invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Invitation introuvable avec l'ID : " + id));

        invitation.setStatus(invitationDto.getStatus());

        InvitationEntity updatedInvitation = invitationRepository.save(invitation);
        
        return mapToPostOutputDto(updatedInvitation);
    }

    // Méthode helper de mapping réutilisable
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
