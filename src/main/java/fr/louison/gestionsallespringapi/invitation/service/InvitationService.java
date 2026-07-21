package fr.louison.gestionsallespringapi.invitation.service;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.invitation.dto.InvitationDto;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.invitation.repository.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<InvitationDto.PostOutputDto> getInvitationsByBooking(Long id) throws Exception {

        bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("Booking not found"));

        List<InvitationEntity> invitations = invitationRepository.findByBooking(id);

        return invitations.stream()
                .map(invitation -> InvitationDto.PostOutputDto.builder()
                        .id(invitation.getIdInvitation())
                        .status(invitation.getStatus())
                        .booking(invitation.getBooking())
                        .attendee(invitation.getEmployeeAttendee())
                        .build()
                )
                .toList();
    }

    public InvitationEntity updateInvitation(Long id, InvitationDto.PutInputDto invitationDto) throws Exception {

        InvitationEntity invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new Exception("Invitation not found"));

        invitation.setStatus(invitationDto.getStatus());

        return invitationRepository.save(invitation);
    }
}
