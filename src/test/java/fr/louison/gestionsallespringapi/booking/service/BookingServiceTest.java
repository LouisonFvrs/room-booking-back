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
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.recurrence.model.enums.RecurrenceType;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import fr.louison.gestionsallespringapi.recurrence.service.RecurrenceService;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    private static final long ROOM_ID = 10L;
    private static final long RECURRENCE_ID = 20L;
    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 7, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2030, 1, 7, 11, 0);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private RecurrenceRepository recurrenceRepository;

    private BookingService bookingService;

    private final EmployeeEntity organizer = employee(1L, EmployeeRole.ROLE_MANAGER);
    private final EmployeeEntity otherManager = employee(2L, EmployeeRole.ROLE_MANAGER);
    private final EmployeeEntity admin = employee(3L, EmployeeRole.ROLE_ADMIN);
    private final RoomEntity room = RoomEntity.builder().idRoom(ROOM_ID).name("Everest").build();

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                roomRepository,
                employeeRepository,
                recurrenceRepository,
                new RecurrenceService(recurrenceRepository)
        );

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        List.of(organizer, otherManager, admin).forEach(employee ->
                when(employeeRepository.findById(employee.getIdEmployee())).thenReturn(Optional.of(employee)));
        when(bookingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createUsesTheAuthenticatedEmployeeAsOrganizer() {
        BookingDto.PostOutputDto created = bookingService.create(input(null), organizer.getIdEmployee());

        assertThat(created.getOrganizer().getId()).isEqualTo(organizer.getIdEmployee());
        assertThat(created.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void createWithRecurrenceGeneratesOneBookingPerOccurrence() {
        givenWeeklyRecurrenceUntil(LocalDate.of(2030, 1, 21));

        bookingService.create(input(RECURRENCE_ID), organizer.getIdEmployee());

        List<BookingEntity> saved = capturedSavedBookings();
        assertThat(saved).extracting(BookingEntity::getStartDate)
                .containsExactly(START, START.plusWeeks(1), START.plusWeeks(2));
        assertThat(saved).extracting(BookingEntity::getEndDate)
                .containsExactly(END, END.plusWeeks(1), END.plusWeeks(2));
        assertThat(saved).allSatisfy(booking ->
                assertThat(booking.getRecurrence().getIdRecurrence()).isEqualTo(RECURRENCE_ID));
    }

    @Test
    void createWithRecurrenceSavesNothingWhenOneOccurrenceIsOccupied() {
        givenWeeklyRecurrenceUntil(LocalDate.of(2030, 1, 21));
        when(bookingRepository.existsOverlappingBooking(ROOM_ID, START.plusWeeks(1), END.plusWeeks(1))).thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(input(RECURRENCE_ID), organizer.getIdEmployee()))
                .isInstanceOf(RessourceConflictException.class)
                .hasMessageContaining("14/01/2030 10:00");

        verify(bookingRepository, never()).saveAll(anyList());
    }

    @Test
    void createRejectsARecurrenceAlreadyAttachedToASeries() {
        givenWeeklyRecurrenceUntil(LocalDate.of(2030, 1, 21));
        when(bookingRepository.existsByRecurrence_IdRecurrence(RECURRENCE_ID)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(input(RECURRENCE_ID), organizer.getIdEmployee()))
                .isInstanceOf(RessourceConflictException.class);
    }

    @Test
    void cancelByTheOrganizerMarksTheBookingCancelled() {
        givenBooking(100L, START, BookingStatus.CONFIRMED);

        BookingDto.PostOutputDto cancelled = bookingService.cancel(100L, organizer.getIdEmployee());

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelByAnAdministratorIsAllowedOnSomeoneElsesBooking() {
        givenBooking(100L, START, BookingStatus.CONFIRMED);

        assertThat(bookingService.cancel(100L, admin.getIdEmployee()).getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelByAnotherManagerIsDenied() {
        BookingEntity booking = givenBooking(100L, START, BookingStatus.CONFIRMED);

        assertThatThrownBy(() -> bookingService.cancel(100L, otherManager.getIdEmployee()))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancelAnAlreadyCancelledBookingIsAConflict() {
        givenBooking(100L, START, BookingStatus.CANCELLED);

        assertThatThrownBy(() -> bookingService.cancel(100L, organizer.getIdEmployee()))
                .isInstanceOf(RessourceConflictException.class);
    }

    @Test
    void updateACancelledBookingIsAConflict() {
        givenBooking(100L, START, BookingStatus.CANCELLED);

        assertThatThrownBy(() -> bookingService.update(input(null), 100L, organizer.getIdEmployee()))
                .isInstanceOf(RessourceConflictException.class);
    }

    @Test
    void updateCannotAttachABookingToAnotherRecurrence() {
        givenBooking(100L, START, BookingStatus.CONFIRMED);

        assertThatThrownBy(() -> bookingService.update(input(RECURRENCE_ID), 100L, organizer.getIdEmployee()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cancelSeriesOnlyCancelsUpcomingConfirmedOccurrences() {
        BookingEntity past = givenBooking(100L, LocalDateTime.now().minusDays(7), BookingStatus.CONFIRMED);
        BookingEntity alreadyCancelled = givenBooking(101L, LocalDateTime.now().plusDays(7), BookingStatus.CANCELLED);
        BookingEntity upcoming = givenBooking(102L, LocalDateTime.now().plusDays(14), BookingStatus.CONFIRMED);
        when(bookingRepository.findAllByRecurrence_IdRecurrenceOrderByStartDateAsc(RECURRENCE_ID))
                .thenReturn(List.of(past, alreadyCancelled, upcoming));

        List<BookingDto.PostOutputDto> cancelled = bookingService.cancelSeries(RECURRENCE_ID, organizer.getIdEmployee());

        assertThat(cancelled).extracting(BookingDto.PostOutputDto::getId).containsExactly(102L);
        assertThat(past.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(upcoming.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    private void givenWeeklyRecurrenceUntil(LocalDate endDate) {
        RecurrenceEntity recurrence = RecurrenceEntity.builder()
                .idRecurrence(RECURRENCE_ID)
                .type(RecurrenceType.WEEKLY)
                .intervalValue(1)
                .endDate(endDate)
                .build();
        when(recurrenceRepository.findById(RECURRENCE_ID)).thenReturn(Optional.of(recurrence));
    }

    private BookingEntity givenBooking(Long id, LocalDateTime start, BookingStatus status) {
        BookingEntity booking = BookingEntity.builder()
                .idBooking(id)
                .title("Point hebdo")
                .startDate(start)
                .endDate(start.plusHours(1))
                .status(status)
                .room(room)
                .employeeOrganizer(organizer)
                .invitations(new ArrayList<>())
                .build();
        when(bookingRepository.findById(eq(id))).thenReturn(Optional.of(booking));
        return booking;
    }

    private BookingDto.PostInputDto input(Long idRecurrence) {
        return BookingDto.PostInputDto.builder()
                .title("Point hebdo")
                .startDate(START)
                .endDate(END)
                .idRoom(ROOM_ID)
                .idRecurrence(idRecurrence)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<BookingEntity> capturedSavedBookings() {
        ArgumentCaptor<List<BookingEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookingRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private static EmployeeEntity employee(Long id, EmployeeRole role) {
        return EmployeeEntity.builder()
                .idEmployee(id)
                .firstName("Prénom" + id)
                .lastName("Nom" + id)
                .email("employee" + id + "@test.local")
                .role(role)
                .build();
    }
}
