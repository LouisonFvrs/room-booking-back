package fr.louison.gestionsallespringapi.demo.service;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DemoDataSeederTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 23);

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private DemoDataSeeder seeder;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(2, 0).atZone(PARIS).toInstant(), PARIS);
        seeder = new DemoDataSeeder(new DemoAccountService(true, "un-mot-de-passe"), employeeRepository, departmentRepository,
                roomRepository, equipmentRepository, bookingRepository, passwordEncoder, clock);

        // Repositories hand back what they are given, as a real save would
        when(employeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(departmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(equipmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(departmentRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
    }

    // CA-5-01: an empty installation receives the whole initial data set
    @Test
    void anEmptyInstallationReceivesThreeAccountsThreeRoomsThreeEquipmentsAndSixMeetings() {
        when(roomRepository.count()).thenReturn(0L);

        seeder.seed();

        ArgumentCaptor<EmployeeEntity> accounts = ArgumentCaptor.forClass(EmployeeEntity.class);
        verify(employeeRepository, times(3)).save(accounts.capture());
        assertThat(accounts.getAllValues()).extracting(EmployeeEntity::getEmail)
                .containsExactly(DemoAccountService.ADMIN_EMAIL, DemoAccountService.MANAGER_EMAIL, DemoAccountService.EMPLOYEE_EMAIL);
        verify(roomRepository, times(3)).save(any());
        verify(equipmentRepository, times(3)).save(any());
        verify(bookingRepository, times(6)).save(any());
    }

    // CA-5-03: meetings are dated from the seeding day and the day after
    @Test
    void meetingsAreDatedTodayAndTomorrow() {
        when(roomRepository.count()).thenReturn(0L);

        seeder.seed();

        ArgumentCaptor<BookingEntity> meetings = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingRepository, times(6)).save(meetings.capture());
        assertThat(meetings.getAllValues()).extracting(booking -> booking.getStartDate().toLocalDate())
                .containsExactly(TODAY, TODAY, TODAY, TODAY, TODAY.plusDays(1), TODAY.plusDays(1));
    }

    // Alternative: an installation that already has rooms only receives the demo accounts
    @Test
    void anInstallationWithRoomsOnlyReceivesTheAccounts() {
        when(roomRepository.count()).thenReturn(4L);

        seeder.seed();

        verify(employeeRepository, times(3)).save(any());
        verify(roomRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }
}
