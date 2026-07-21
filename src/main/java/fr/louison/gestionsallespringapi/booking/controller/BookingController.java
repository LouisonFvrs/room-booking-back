package fr.louison.gestionsallespringapi.booking.controller;

import fr.louison.gestionsallespringapi.booking.dto.BookingDto;
import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/{id}")
    public BookingDto.PostOutputDto getBooking(@PathVariable Long id){
        return bookingService.getBooking(id);
    }

    @GetMapping("/organisateur/{id}")
    public List<BookingDto.PostOutputDto> getBookingByOrganizer(@PathVariable Long id){
        return bookingService.getByOrganizer(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id){
        return bookingService.delete(id);
    }

    //Créer une réservation, attention à la disponibilité
    @PostMapping("/create")
    public BookingDto.PostOutputDto create(@Valid @RequestBody BookingDto.PostInputDto bookingDto){
        BookingEntity booking = bookingService.create(bookingDto);

        return BookingDto.PostOutputDto.builder()
                .id(booking.getIdBooking())
                .title(booking.getTitle())
                .description(booking.getDescription())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .room(BookingDto.RoomSummaryDto.builder()
                        .id(booking.getRoom().getIdRoom())
                        .name(booking.getRoom().getName())
                        .build())
                .status(booking.getStatus())
                .organizer(BookingDto.EmployeeSummaryDto.builder()
                        .id(booking.getEmployeeOrganizer().getIdEmployee())
                        .firstName(booking.getEmployeeOrganizer().getFirstName())
                        .lastName(booking.getEmployeeOrganizer().getLastName())
                        .build())
                .recurrence(booking.getRecurrence())
                .invitations(booking.getInvitations().stream()
                        .map(invitation -> BookingDto.InvitationSummaryDto.builder()
                                .id(invitation.getIdInvitation())
                                .status(invitation.getStatus())
                                .attendeeId(invitation.getEmployeeAttendee().getIdEmployee())
                                .build()
                        )
                        .toList()
                )
                .build();
    }

    @PutMapping("/{id}")
    public BookingDto.PostOutputDto update(@Valid @RequestBody BookingDto.PostInputDto bookingDto, @PathVariable Long id){
        BookingEntity booking = bookingService.update(bookingDto, id);

        return BookingDto.PostOutputDto.builder()
                .id(booking.getIdBooking())
                .title(booking.getTitle())
                .description(booking.getDescription())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .room(BookingDto.RoomSummaryDto.builder()
                        .id(booking.getRoom().getIdRoom())
                        .name(booking.getRoom().getName())
                        .build())
                .status(booking.getStatus())
                .organizer(BookingDto.EmployeeSummaryDto.builder()
                        .id(booking.getEmployeeOrganizer().getIdEmployee())
                        .firstName(booking.getEmployeeOrganizer().getFirstName())
                        .lastName(booking.getEmployeeOrganizer().getLastName())
                        .build())
                .recurrence(booking.getRecurrence())
                .invitations(booking.getInvitations().stream()
                        .map(invitation -> BookingDto.InvitationSummaryDto.builder()
                                .id(invitation.getIdInvitation())
                                .status(invitation.getStatus())
                                .attendeeId(invitation.getEmployeeAttendee().getIdEmployee())
                                .build()
                        )
                        .toList()
                )
                .build();
    }

}
