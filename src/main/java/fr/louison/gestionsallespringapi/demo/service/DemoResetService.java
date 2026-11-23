package fr.louison.gestionsallespringapi.demo.service;

import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puts a demonstration deployment back to its initial state: every visitor can sign in as an
 * administrator, so whatever they changed or deleted is wiped and the demo data set is recreated.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoResetService {

    private final DemoAccountService demoAccountService;
    private final BookingRepository bookingRepository;
    private final RecurrenceRepository recurrenceRepository;
    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DemoDataSeeder demoDataSeeder;

    /**
     * One transaction for the whole reset (RG-5-05): if the seeding fails, the deletions are rolled
     * back and visitors keep the previous data rather than an empty application.
     */
    @Transactional
    public void reset() {
        // RG-5-01: a real installation must never lose its data
        if (!demoAccountService.isEnabled()) {
            return;
        }

        // RG-5-02: rows that hold a foreign key go before the rows they point to. deleteAll() loads and
        // removes each entity, so bookings take their invitations along (cascade) and rooms clear the
        // room_equipment join table they own; deleteAllInBatch() would skip both and break on the keys.
        bookingRepository.deleteAll();
        recurrenceRepository.deleteAll();
        roomRepository.deleteAll();
        equipmentRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        // Hibernate sends inserts before deletes when it flushes: without this, the recreated demo accounts
        // could reach the database while the old ones still hold their unique e-mails
        departmentRepository.flush();

        // RG-5-03, RG-5-04: the initial data set, its meetings dated from today
        demoDataSeeder.seed();

        log.info("Demo data reset to its initial state.");
    }
}
