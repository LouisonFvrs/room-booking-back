package fr.louison.gestionsallespringapi.invitation.controller;

import fr.louison.gestionsallespringapi.auth.CustomUserDetails;
import fr.louison.gestionsallespringapi.invitation.dto.InvitationDto;
import fr.louison.gestionsallespringapi.invitation.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitation")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<InvitationDto.PostOutputDto>> getMyInvitations(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(
                invitationService.getInvitationsByAttendee(pageable, currentUser.getEmployee().getIdEmployee())
        );
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Page<InvitationDto.PostOutputDto>> getInvitationsByBooking(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(invitationService.getInvitationsByBooking(pageable, bookingId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<InvitationDto.PostOutputDto> updateInvitation(
            @PathVariable Long id,
            @Valid @RequestBody InvitationDto.PutInputDto invitationDto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(
                invitationService.updateInvitation(id, invitationDto, currentUser.getEmployee().getIdEmployee())
        );
    }
}
