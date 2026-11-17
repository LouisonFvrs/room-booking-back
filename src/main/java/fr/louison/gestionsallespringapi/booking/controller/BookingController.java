package fr.louison.gestionsallespringapi.booking.controller;

import fr.louison.gestionsallespringapi.auth.CustomUserDetails;
import fr.louison.gestionsallespringapi.booking.dto.BookingDto;
import fr.louison.gestionsallespringapi.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<BookingDto.PostOutputDto> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBooking(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    @GetMapping("/me")
    public ResponseEntity<List<BookingDto.PostOutputDto>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.getByOrganizer(currentEmployeeId(currentUser)));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/organisateur/{id}")
    public ResponseEntity<List<BookingDto.PostOutputDto>> getBookingByOrganizer(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getByOrganizer(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        bookingService.delete(id, currentEmployeeId(currentUser));
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BookingDto.PostOutputDto> create(
            @Valid @RequestBody BookingDto.PostInputDto bookingDto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        BookingDto.PostOutputDto createdBooking = bookingService.create(bookingDto, currentEmployeeId(currentUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BookingDto.PostOutputDto> update(
            @Valid @RequestBody BookingDto.PostInputDto bookingDto,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.update(bookingDto, id, currentEmployeeId(currentUser)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BookingDto.PostOutputDto> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.cancel(id, currentEmployeeId(currentUser)));
    }

    @GetMapping("/recurrence/{idRecurrence}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<BookingDto.PostOutputDto>> getSeries(@PathVariable Long idRecurrence) {
        return ResponseEntity.ok(bookingService.getByRecurrence(idRecurrence));
    }

    @PatchMapping("/recurrence/{idRecurrence}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<BookingDto.PostOutputDto>> cancelSeries(
            @PathVariable Long idRecurrence,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.cancelSeries(idRecurrence, currentEmployeeId(currentUser)));
    }

    private Long currentEmployeeId(CustomUserDetails currentUser) {
        return currentUser.getEmployee().getIdEmployee();
    }
}
