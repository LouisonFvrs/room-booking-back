package fr.louison.gestionsallespringapi.demo;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import fr.louison.gestionsallespringapi.booking.model.enums.BookingStatus;
import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.demo.service.DemoAccountService;
import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills a demonstration deployment with accounts, rooms and a few meetings so that a visitor
 * has something to look at. It runs once: an existing demo administrator means the data is there.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final DemoAccountService demoAccountService;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoAccountService.isEnabled() || employeeRepository.existsByEmail(DemoAccountService.ADMIN_EMAIL)) {
            return;
        }

        DepartmentEntity management = department("Direction");
        DepartmentEntity product = department("Produit");

        EmployeeEntity admin = employee("Camille", "Durand", DemoAccountService.ADMIN_EMAIL, EmployeeRole.ROLE_ADMIN, management);
        EmployeeEntity manager = employee("Sofia", "Marchand", DemoAccountService.MANAGER_EMAIL, EmployeeRole.ROLE_MANAGER, product);
        EmployeeEntity employee = employee("Théo", "Garnier", DemoAccountService.EMPLOYEE_EMAIL, EmployeeRole.ROLE_EMPLOYEE, product);

        // Rooms and meetings are only added to an empty installation: on one that already has rooms,
        // the demo accounts are enough and existing data is left alone.
        if (roomRepository.count() == 0) {
            createRoomsAndMeetings(admin, manager, employee);
        }

        log.warn("Demo mode is ON: demo accounts exist and their password is served by /api/auth/demo-accounts.");
    }

    private void createRoomsAndMeetings(EmployeeEntity admin, EmployeeEntity manager, EmployeeEntity employee) {
        RoomEntity large = room("Salle Atlas", 16);
        RoomEntity medium = room("Salle Horizon", 8);
        RoomEntity small = room("Salle Boréale", 5);

        equipment("Écran 4K", "ECR-001", large, medium);
        equipment("Visioconférence", "VIS-001", large);
        equipment("Tableau blanc", "TAB-001", medium, small);

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        booking("Revue de sprint", today, "09:00", "10:30", large, manager, List.of(invited(employee, InvitationStatus.ACCEPTED), invited(admin, InvitationStatus.PENDING)));
        booking("Point budget", today, "11:00", "12:00", medium, admin, List.of(invited(manager, InvitationStatus.ACCEPTED)));
        booking("Atelier design", today, "14:00", "15:30", medium, manager, List.of(invited(employee, InvitationStatus.PENDING)));
        booking("Entretien annuel", today, "16:00", "17:00", small, admin, List.of());
        booking("Comité produit", tomorrow, "10:00", "11:30", large, admin, List.of(invited(manager, InvitationStatus.PENDING), invited(employee, InvitationStatus.PENDING)));
        booking("Démonstration client", tomorrow, "14:00", "15:00", large, manager, List.of(invited(employee, InvitationStatus.ACCEPTED)));
    }

    private DepartmentEntity department(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> departmentRepository.save(DepartmentEntity.builder().name(name).build()));
    }

    private EmployeeEntity employee(String firstName, String lastName, String email, EmployeeRole role, DepartmentEntity department) {
        return employeeRepository.save(EmployeeEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(demoAccountService.getPassword()))
                .role(role)
                .department(department)
                .build());
    }

    private RoomEntity room(String name, int capacity) {
        return roomRepository.save(RoomEntity.builder()
                .name(name)
                .capacity(capacity)
                .isAvailable(true)
                .equipments(new ArrayList<>())
                .build());
    }

    private void equipment(String name, String serialNumber, RoomEntity... rooms) {
        EquipmentEntity equipment = equipmentRepository.save(EquipmentEntity.builder()
                .name(name)
                .serialNumber(serialNumber)
                .rooms(new ArrayList<>())
                .build());

        for (RoomEntity room : rooms) {
            room.addEquipment(equipment);
        }
    }

    private InvitationEntity invited(EmployeeEntity attendee, InvitationStatus status) {
        return InvitationEntity.builder().employeeAttendee(attendee).status(status).build();
    }

    private void booking(String title, LocalDate day, String start, String end, RoomEntity room, EmployeeEntity organizer, List<InvitationEntity> invitations) {
        BookingEntity booking = BookingEntity.builder()
                .title(title)
                .startDate(day.atTime(LocalTime.parse(start)))
                .endDate(day.atTime(LocalTime.parse(end)))
                .status(BookingStatus.CONFIRMED)
                .room(room)
                .employeeOrganizer(organizer)
                .invitations(new ArrayList<>())
                .build();

        invitations.forEach(invitation -> {
            invitation.setBooking(booking);
            booking.getInvitations().add(invitation);
        });
        bookingRepository.save(booking);
    }
}
