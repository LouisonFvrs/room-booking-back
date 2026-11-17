package fr.louison.gestionsallespringapi.booking.service;

import fr.louison.gestionsallespringapi.booking.dto.BookingDto;
import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.model.enums.BookingStatus;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.exception.BadRequestException;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import fr.louison.gestionsallespringapi.exception.RessourceNotFoundException;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;
    private final RecurrenceRepository recurrenceRepository;

    @Transactional(readOnly = true)
    public BookingDto.PostOutputDto getBooking(Long id) {
        return mapToPostOutputDto(findBooking(id));
    }

    @Transactional(readOnly = true)
    public List<BookingDto.PostOutputDto> getByOrganizer(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RessourceNotFoundException("Employé/Organisateur introuvable avec l'ID : " + id);
        }

        return bookingRepository.findAllByEmployeeOrganizer_IdEmployee(id).stream()
                .map(this::mapToPostOutputDto)
                .toList();
    }

    public void delete(Long id, Long currentEmployeeId) {
        BookingEntity booking = findBooking(id);
        assertCanManage(booking, findEmployee(currentEmployeeId));

        bookingRepository.delete(booking);
    }

    public BookingDto.PostOutputDto create(BookingDto.PostInputDto bookingDto, Long currentEmployeeId) {
        assertValidSlot(bookingDto.getStartDate(), bookingDto.getEndDate());

        RoomEntity room = findRoom(bookingDto.getIdRoom());
        EmployeeEntity organizer = findEmployee(currentEmployeeId);
        RecurrenceEntity recurrence = bookingDto.getIdRecurrence() != null
                ? findRecurrence(bookingDto.getIdRecurrence())
                : null;

        if (bookingRepository.existsOverlappingBooking(room.getIdRoom(), bookingDto.getStartDate(), bookingDto.getEndDate())) {
            throw new RessourceConflictException("La salle " + room.getName() + " est déjà réservée sur ce créneau horaire.");
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
                .invitations(new ArrayList<>())
                .build();
        booking.getInvitations().addAll(buildInvitations(booking, bookingDto.getInvitationIds()));

        return mapToPostOutputDto(bookingRepository.save(booking));
    }

    // Cancelling keeps the booking as history and frees the slot: overlap checks
    // ignore cancelled bookings.
    public BookingDto.PostOutputDto cancel(Long id, Long currentEmployeeId) {
        BookingEntity booking = findBooking(id);
        assertCanManage(booking, findEmployee(currentEmployeeId));
        assertNotCancelled(booking);

        booking.setStatus(BookingStatus.CANCELLED);

        return mapToPostOutputDto(bookingRepository.save(booking));
    }

    public BookingDto.PostOutputDto update(BookingDto.PostInputDto bookingDto, Long idBooking, Long currentEmployeeId) {
        assertValidSlot(bookingDto.getStartDate(), bookingDto.getEndDate());

        BookingEntity booking = findBooking(idBooking);
        assertCanManage(booking, findEmployee(currentEmployeeId));
        assertNotCancelled(booking);

        RoomEntity room = findRoom(bookingDto.getIdRoom());

        boolean isOccupied = bookingRepository.existsOverlappingBookingExcludingCurrent(
                room.getIdRoom(),
                idBooking,
                bookingDto.getStartDate(),
                bookingDto.getEndDate()
        );
        if (isOccupied) {
            throw new RessourceConflictException("La salle " + room.getName() + " est occupée sur ce créneau.");
        }

        RecurrenceEntity recurrence = bookingDto.getIdRecurrence() != null
                ? findRecurrence(bookingDto.getIdRecurrence())
                : null;

        booking.setTitle(bookingDto.getTitle());
        booking.setDescription(bookingDto.getDescription());
        booking.setStartDate(bookingDto.getStartDate());
        booking.setEndDate(bookingDto.getEndDate());
        booking.setRoom(room);
        booking.setRecurrence(recurrence);

        if (bookingDto.getInvitationIds() != null) {
            booking.getInvitations().clear();
            booking.getInvitations().addAll(buildInvitations(booking, bookingDto.getInvitationIds()));
        }

        return mapToPostOutputDto(bookingRepository.save(booking));
    }

    private void assertValidSlot(LocalDateTime startDate, LocalDateTime endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new BadRequestException("La date de fin doit être strictement supérieure à la date de début.");
        }
    }

    private void assertNotCancelled(BookingEntity booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RessourceConflictException("La réservation " + booking.getIdBooking() + " est annulée.");
        }
    }

    // Only the organizer or an administrator may change a booking, whatever role
    // gave access to the endpoint.
    private void assertCanManage(BookingEntity booking, EmployeeEntity currentEmployee) {
        boolean isOrganizer = booking.getEmployeeOrganizer() != null
                && booking.getEmployeeOrganizer().getIdEmployee().equals(currentEmployee.getIdEmployee());

        if (!isOrganizer && currentEmployee.getRole() != EmployeeRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Seul l'organisateur ou un administrateur peut modifier cette réservation.");
        }
    }

    private List<InvitationEntity> buildInvitations(BookingEntity booking, List<Long> attendeeIds) {
        if (attendeeIds == null) {
            return List.of();
        }

        return attendeeIds.stream()
                .distinct()
                .map(attendeeId -> InvitationEntity.builder()
                        .status(InvitationStatus.PENDING)
                        .booking(booking)
                        .employeeAttendee(employeeRepository.findById(attendeeId)
                                .orElseThrow(() -> new RessourceNotFoundException("Invité introuvable avec l'ID : " + attendeeId)))
                        .build())
                .toList();
    }

    private BookingEntity findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Réservation introuvable avec l'ID : " + id));
    }

    private RoomEntity findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Salle introuvable avec l'ID : " + id));
    }

    private EmployeeEntity findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Employé introuvable avec l'ID : " + id));
    }

    private RecurrenceEntity findRecurrence(Long id) {
        return recurrenceRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Récurrence introuvable avec l'ID : " + id));
    }

    private BookingDto.PostOutputDto mapToPostOutputDto(BookingEntity booking) {
        return BookingDto.PostOutputDto.builder()
                .id(booking.getIdBooking())
                .title(booking.getTitle())
                .description(booking.getDescription())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus())
                .recurrence(booking.getRecurrence())
                .room(booking.getRoom() != null
                        ? BookingDto.RoomSummaryDto.builder()
                                .id(booking.getRoom().getIdRoom())
                                .name(booking.getRoom().getName())
                                .build()
                        : null)
                .organizer(booking.getEmployeeOrganizer() != null
                        ? BookingDto.EmployeeSummaryDto.builder()
                                .id(booking.getEmployeeOrganizer().getIdEmployee())
                                .firstName(booking.getEmployeeOrganizer().getFirstName())
                                .lastName(booking.getEmployeeOrganizer().getLastName())
                                .build()
                        : null)
                .invitations(booking.getInvitations() != null
                        ? booking.getInvitations().stream()
                                .map(invitation -> BookingDto.InvitationSummaryDto.builder()
                                        .id(invitation.getIdInvitation())
                                        .status(invitation.getStatus())
                                        .attendeeId(invitation.getEmployeeAttendee().getIdEmployee())
                                        .build())
                                .toList()
                        : List.of())
                .build();
    }
}
