package fr.louison.gestionsallespringapi.booking.controller;

import fr.louison.gestionsallespringapi.booking.dto.BookingDto;
import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<BookingDto.PostOutputDto> getBooking(@PathVariable Long id){
        BookingDto.PostOutputDto booking = bookingService.getBooking(id);
        return ResponseEntity.ok(booking);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/organisateur/{id}")
    public ResponseEntity<List<BookingDto.PostOutputDto>> getBookingByOrganizer(@PathVariable Long id){
        List<BookingDto.PostOutputDto> bookings = bookingService.getByOrganizer(id);
        return ResponseEntity.ok(bookings);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id){
        bookingService.delete(id);
        return  ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BookingDto.PostOutputDto> create(@Valid @RequestBody BookingDto.PostInputDto bookingDto) {
        BookingDto.PostOutputDto createdBooking = bookingService.create(bookingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BookingDto.PostOutputDto> update(
            @Valid @RequestBody BookingDto.PostInputDto bookingDto,
            @PathVariable Long id
    ) {
        BookingDto.PostOutputDto updatedBooking = bookingService.update(bookingDto, id);
        return ResponseEntity.ok(updatedBooking);
    }

}
