package fr.louison.gestionsallespringapi.invitation.controller;

import fr.louison.gestionsallespringapi.invitation.dto.InvitationDto;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitation")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Page<InvitationDto.PostOutputDto>> getInvitationsByBooking(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @PathVariable Long bookingId
    ) {
        Page<InvitationDto.PostOutputDto> invitations = invitationService.getInvitationsByBooking(pageable, bookingId);
        return ResponseEntity.ok(invitations);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<InvitationDto.PostOutputDto> updateInvitation(
            @PathVariable Long id,
            @Valid @RequestBody InvitationDto.PutInputDto invitationDto
    ) {
        InvitationDto.PostOutputDto updatedInvitation = invitationService.updateInvitation(id, invitationDto);
        return ResponseEntity.ok(updatedInvitation);
    }
}
