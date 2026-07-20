package fr.louison.gestionsallespringapi.invitation.controller;

import fr.louison.gestionsallespringapi.invitation.dto.InvitationDto;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitation")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @GetMapping("/{id}")
    public List<InvitationDto.PostOutputDto> getInvitationsByBooking(@PathVariable Long id) throws Exception {
        return invitationService.getInvitationsByBooking(id);
    }

    @PostMapping("/create")
    public InvitationDto.PostOutputDto createInvitation(@Valid @RequestBody InvitationDto.PostInputDto invitationDto) throws Exception {
        InvitationEntity invitation = invitationService.createInvitation(invitationDto);
        return InvitationDto.PostOutputDto.builder()
                .id(invitation.getIdInvitation())
                .status(invitation.getStatus())
                .booking(invitation.getBooking())
                .attendee(invitation.getEmployeeAttendee())
                .build();
    }

    @PutMapping("/{id}")
    public InvitationDto.PostOutputDto updateInvitation(@Valid @RequestBody InvitationDto.PutInputDto invitationDto, @PathVariable Long id) throws Exception {
        InvitationEntity invitation = invitationService.updateInvitation(id, invitationDto);
        return InvitationDto.PostOutputDto.builder()
                .id(invitation.getIdInvitation())
                .status(invitation.getStatus())
                .booking(invitation.getBooking())
                .attendee(invitation.getEmployeeAttendee())
                .build();
    }
}
