package fr.louison.gestionsallespringapi.booking.service;

import fr.louison.gestionsallespringapi.booking.dto.BookingDto;
import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.model.enums.BookingStatus;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RecurrenceRepository recurrenceRepository;

    public BookingDto.PostOutputDto getBooking(Long id){
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        return BookingDto.PostOutputDto.builder()
                .id(booking.getIdBooking())
                .title(booking.getTitle())
                .description(booking.getDescription())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus())
                .recurrence(booking.getRecurrence())
                .room(booking.getRoom() != null ? BookingDto.RoomSummaryDto.builder()
                                                  .id(booking.getRoom().getIdRoom())
                                                  .name(booking.getRoom().getName())
                                                  .build() : null)
                .organizer(booking.getEmployeeOrganizer() != null ? BookingDto.EmployeeSummaryDto.builder()
                                                                    .id(booking.getEmployeeOrganizer().getIdEmployee())
                                                                    .firstName(booking.getEmployeeOrganizer().getFirstName())
                                                                    .lastName(booking.getEmployeeOrganizer().getLastName())
                                                                    .build() : null)
                .invitations(booking.getInvitations().stream()
                        .map(invitation -> BookingDto.InvitationSummaryDto.builder()
                                .id(invitation.getIdInvitation())
                                .status(invitation.getStatus())
                                .attendeeId(invitation.getEmployeeAttendee().getIdEmployee())
                                .build())
                        .toList())
                .build();
    }

    public List< BookingDto.PostOutputDto> getByOrganizer(Long id){

        List<BookingEntity> bookings = bookingRepository.findAllByEmployeeOrganizer_IdEmployee(id);

        if(bookings.isEmpty()){ throw new EntityNotFoundException("Booking not found"); }

        return bookings.stream()
                .map(booking -> BookingDto.PostOutputDto.builder()
                        .id(booking.getIdBooking())
                        .title(booking.getTitle())
                        .description(booking.getDescription())
                        .startDate(booking.getStartDate())
                        .endDate(booking.getEndDate())
                        .room(booking.getRoom() != null ? BookingDto.RoomSummaryDto.builder()
                                                          .id(booking.getRoom().getIdRoom())
                                                          .name(booking.getRoom().getName())
                                                          .build() : null)
                        .status(booking.getStatus())
                        .organizer(booking.getEmployeeOrganizer() != null ? BookingDto.EmployeeSummaryDto.builder()
                                                                            .id(booking.getEmployeeOrganizer().getIdEmployee())
                                                                            .firstName(booking.getEmployeeOrganizer().getFirstName())
                                                                            .lastName(booking.getEmployeeOrganizer().getLastName())
                                                                            .build() : null)
                        .recurrence(booking.getRecurrence())
                        .invitations(booking.getInvitations().stream()
                                .map(invitation -> BookingDto.InvitationSummaryDto.builder()
                                        .id(invitation.getIdInvitation())
                                        .status(invitation.getStatus())
                                        .attendeeId(invitation.getEmployeeAttendee().getIdEmployee())
                                        .build())
                                .toList())
                        .build()
                )
                .toList();
    }

    public ResponseEntity<String> delete(Long id){
        bookingRepository.deleteById(id);
        return ResponseEntity.ok("Réservation supprimé");
    }

    public BookingEntity create(BookingDto.PostInputDto bookingDto) {

        if (bookingDto.getEndDate().isBefore(bookingDto.getStartDate())
                || bookingDto.getEndDate().isEqual(bookingDto.getStartDate())) {
            throw new IllegalArgumentException("La date de fin doit être strictement supérieure à la date de début.");
        }

        RoomEntity room = roomRepository.findById(bookingDto.getIdRoom())
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + bookingDto.getIdRoom()));

        boolean isOccupied = bookingRepository.existsOverlappingBooking(
                bookingDto.getIdRoom(),
                bookingDto.getStartDate(),
                bookingDto.getEndDate()
        );

        if (isOccupied) {
            throw new IllegalStateException("La salle " + room.getName() + " est déjà réservée sur ce créneau horaire.");
        }

        EmployeeEntity organizer = employeeRepository.findById(bookingDto.getIdOrganizer())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + bookingDto.getIdOrganizer()));

        RecurrenceEntity recurrence = null;
        if (bookingDto.getIdRecurrence() != null) {
            recurrence = recurrenceRepository.findById(bookingDto.getIdRecurrence())
                    .orElseThrow(() -> new EntityNotFoundException("Recurrence not found with id: " + bookingDto.getIdRecurrence()));
        }

        BookingEntity booking = BookingEntity.builder()
                .title(bookingDto.getTitle())
                .description(bookingDto.getDescription())
                .startDate(bookingDto.getStartDate())
                .endDate(bookingDto.getEndDate())
                .status(BookingStatus.CONFIRMED)
                .room(room)
                .employeeOrganizer(organizer)
                .recurrence(recurrence)
                .build();

        if (bookingDto.getInvitationIds() != null && !bookingDto.getInvitationIds().isEmpty()) {
            List<InvitationEntity> invitations = bookingDto.getInvitationIds().stream()
                    .map(userId -> {
                        EmployeeEntity attendee = employeeRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("Attendee not found with id: " + userId));

                        return InvitationEntity.builder()
                                .status(InvitationStatus.PENDING)
                                .booking(booking)
                                .employeeAttendee(attendee)
                                .build();
                    })
                    .toList();

            booking.getInvitations().addAll(invitations);
        }

        return bookingRepository.save(booking);
    }

    public BookingEntity update(BookingDto.PostInputDto bookingDto, Long idBooking) {

        if (bookingDto.getEndDate().isBefore(bookingDto.getStartDate())
                || bookingDto.getEndDate().isEqual(bookingDto.getStartDate())) {
            throw new IllegalArgumentException("La date de fin doit être supérieure à la date de début.");
        }

        BookingEntity booking = bookingRepository.findById(idBooking)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + idBooking));

        RoomEntity room = roomRepository.findById(bookingDto.getIdRoom())
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + bookingDto.getIdRoom()));

        boolean isOccupied = bookingRepository.existsOverlappingBookingExcludingCurrent(
                bookingDto.getIdRoom(),
                idBooking,
                bookingDto.getStartDate(),
                bookingDto.getEndDate()
        );

        if (isOccupied) {
            throw new IllegalStateException("La salle " + room.getName() + " est occupée sur ce créneau.");
        }

        EmployeeEntity organizer = employeeRepository.findById(bookingDto.getIdOrganizer())
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found with id: " + bookingDto.getIdOrganizer()));

        RecurrenceEntity recurrence = null;
        if (bookingDto.getIdRecurrence() != null) {
            recurrence = recurrenceRepository.findById(bookingDto.getIdRecurrence())
                    .orElseThrow(() -> new EntityNotFoundException("Recurrence not found with id: " + bookingDto.getIdRecurrence()));
        }

        booking.setTitle(bookingDto.getTitle());
        booking.setDescription(bookingDto.getDescription());
        booking.setStartDate(bookingDto.getStartDate());
        booking.setEndDate(bookingDto.getEndDate());
        booking.setRoom(room);
        booking.setEmployeeOrganizer(organizer);
        booking.setRecurrence(recurrence);

        if (bookingDto.getInvitationIds() != null) {
            booking.getInvitations().clear();

            List<InvitationEntity> newInvitations = bookingDto.getInvitationIds().stream()
                    .map(employeeId -> {
                        EmployeeEntity attendee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new EntityNotFoundException("Attendee not found with id: " + employeeId));

                        return InvitationEntity.builder()
                                .status(InvitationStatus.PENDING)
                                .booking(booking)
                                .employeeAttendee(attendee)
                                .build();
                    })
                    .toList();

            booking.getInvitations().addAll(newInvitations);
        }

        return bookingRepository.save(booking);
    }


}
