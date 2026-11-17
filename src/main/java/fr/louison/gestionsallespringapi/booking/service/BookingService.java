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
import fr.louison.gestionsallespringapi.recurrence.service.RecurrenceService;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final RecurrenceService recurrenceService;

    private static final DateTimeFormatter CONFLICT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

    @Transactional(readOnly = true)
    public List<BookingDto.PostOutputDto> getByRecurrence(Long idRecurrence) {
        return findSeries(idRecurrence).stream()
                .map(this::mapToPostOutputDto)
                .toList();
    }

    /** Bookings that are not cancelled and overlap the given day, for every room. */
    @Transactional(readOnly = true)
    public List<BookingDto.PostOutputDto> getPlanning(LocalDate day) {
        return bookingRepository.findActiveBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()).stream()
                .map(this::mapToPostOutputDto)
                .toList();
    }

    /**
     * Tells, for a slot and each occurrence of its optional recurrence, whether the room is free.
     * Nothing is stored: it lets a client warn about conflicts before creating the booking.
     */
    @Transactional(readOnly = true)
    public BookingDto.AvailabilityOutputDto checkAvailability(BookingDto.AvailabilityInputDto input) {
        assertValidSlot(input.getStartDate(), input.getEndDate());
        RoomEntity room = findRoom(input.getIdRoom());

        List<LocalDateTime> starts = input.getRecurrence() != null
                ? recurrenceService.computeOccurrenceStarts(
                        RecurrenceEntity.builder()
                                .type(input.getRecurrence().getType())
                                .intervalValue(input.getRecurrence().getIntervalValue())
                                .endDate(input.getRecurrence().getEndDate())
                                .build(),
                        input.getStartDate())
                : List.of(input.getStartDate());
        Duration duration = Duration.between(input.getStartDate(), input.getEndDate());

        List<BookingDto.SlotDto> slots = starts.stream()
                .map(start -> BookingDto.SlotDto.builder()
                        .startDate(start)
                        .endDate(start.plus(duration))
                        .available(!bookingRepository.existsOverlappingBooking(room.getIdRoom(), start, start.plus(duration)))
                        .build())
                .toList();

        return BookingDto.AvailabilityOutputDto.builder()
                .available(slots.stream().allMatch(BookingDto.SlotDto::isAvailable))
                .slots(slots)
                .build();
    }

    public void delete(Long id, Long currentEmployeeId) {
        BookingEntity booking = findBooking(id);
        assertCanManage(booking, findEmployee(currentEmployeeId));

        bookingRepository.delete(booking);
    }

    /**
     * Creates the booking, or every occurrence of the series when a recurrence is given.
     * The series is all-or-nothing: a single occupied slot rejects the whole request.
     *
     * @return the first occurrence
     */
    public BookingDto.PostOutputDto create(BookingDto.PostInputDto bookingDto, Long currentEmployeeId) {
        assertValidSlot(bookingDto.getStartDate(), bookingDto.getEndDate());

        RoomEntity room = findRoom(bookingDto.getIdRoom());
        EmployeeEntity organizer = findEmployee(currentEmployeeId);
        RecurrenceEntity recurrence = bookingDto.getIdRecurrence() != null
                ? findUnusedRecurrence(bookingDto.getIdRecurrence())
                : null;

        List<LocalDateTime> starts = recurrence != null
                ? recurrenceService.computeOccurrenceStarts(recurrence, bookingDto.getStartDate())
                : List.of(bookingDto.getStartDate());
        Duration duration = Duration.between(bookingDto.getStartDate(), bookingDto.getEndDate());

        assertRoomFree(room, starts, duration);

        List<BookingEntity> bookings = starts.stream()
                .map(start -> {
                    BookingEntity booking = BookingEntity.builder()
                            .title(bookingDto.getTitle())
                            .description(bookingDto.getDescription())
                            .startDate(start)
                            .endDate(start.plus(duration))
                            .status(BookingStatus.CONFIRMED)
                            .room(room)
                            .employeeOrganizer(organizer)
                            .recurrence(recurrence)
                            .invitations(new ArrayList<>())
                            .build();
                    booking.getInvitations().addAll(buildInvitations(booking, bookingDto.getInvitationIds()));
                    return booking;
                })
                .toList();

        return mapToPostOutputDto(bookingRepository.saveAll(bookings).getFirst());
    }

    /**
     * Cancels the occurrences of a series that have not started yet; past ones stay as history.
     */
    public List<BookingDto.PostOutputDto> cancelSeries(Long idRecurrence, Long currentEmployeeId) {
        List<BookingEntity> series = findSeries(idRecurrence);
        assertCanManage(series.getFirst(), findEmployee(currentEmployeeId));

        LocalDateTime now = LocalDateTime.now();
        List<BookingEntity> upcoming = series.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED)
                .filter(booking -> booking.getStartDate().isAfter(now))
                .toList();
        upcoming.forEach(booking -> booking.setStatus(BookingStatus.CANCELLED));

        return bookingRepository.saveAll(upcoming).stream()
                .map(this::mapToPostOutputDto)
                .toList();
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

        assertRecurrenceUnchanged(booking, bookingDto.getIdRecurrence());

        booking.setTitle(bookingDto.getTitle());
        booking.setDescription(bookingDto.getDescription());
        booking.setStartDate(bookingDto.getStartDate());
        booking.setEndDate(bookingDto.getEndDate());
        booking.setRoom(room);

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

    private void assertRoomFree(RoomEntity room, List<LocalDateTime> starts, Duration duration) {
        List<String> occupiedSlots = starts.stream()
                .filter(start -> bookingRepository.existsOverlappingBooking(room.getIdRoom(), start, start.plus(duration)))
                .map(CONFLICT_DATE_FORMAT::format)
                .toList();

        if (!occupiedSlots.isEmpty()) {
            throw new RessourceConflictException(
                    "La salle " + room.getName() + " est déjà réservée sur : " + String.join(", ", occupiedSlots) + "."
            );
        }
    }

    // A series is generated once, at creation. Moving one occurrence is allowed,
    // re-attaching it to another recurrence would leave both series inconsistent.
    private void assertRecurrenceUnchanged(BookingEntity booking, Long requestedIdRecurrence) {
        Long currentIdRecurrence = booking.getRecurrence() != null ? booking.getRecurrence().getIdRecurrence() : null;

        if (requestedIdRecurrence != null && !requestedIdRecurrence.equals(currentIdRecurrence)) {
            throw new BadRequestException("La récurrence d'une réservation existante ne peut pas être modifiée.");
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

    // One recurrence describes one series: sharing it would make "cancel the series"
    // hit bookings that belong to someone else.
    private RecurrenceEntity findUnusedRecurrence(Long id) {
        RecurrenceEntity recurrence = recurrenceRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Récurrence introuvable avec l'ID : " + id));

        if (bookingRepository.existsByRecurrence_IdRecurrence(id)) {
            throw new RessourceConflictException("La récurrence " + id + " est déjà rattachée à une série de réservations.");
        }

        return recurrence;
    }

    private List<BookingEntity> findSeries(Long idRecurrence) {
        List<BookingEntity> series = bookingRepository.findAllByRecurrence_IdRecurrenceOrderByStartDateAsc(idRecurrence);
        if (series.isEmpty()) {
            throw new RessourceNotFoundException("Aucune réservation pour la récurrence avec l'ID : " + idRecurrence);
        }

        return series;
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
