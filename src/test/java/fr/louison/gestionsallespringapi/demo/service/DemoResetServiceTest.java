package fr.louison.gestionsallespringapi.demo.service;

import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.recurrence.repository.RecurrenceRepository;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DemoResetServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RecurrenceRepository recurrenceRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private DemoDataSeeder demoDataSeeder;

    private DemoResetService service(boolean demoEnabled) {
        return new DemoResetService(new DemoAccountService(demoEnabled, "un-mot-de-passe"), bookingRepository, recurrenceRepository,
                roomRepository, equipmentRepository, employeeRepository, departmentRepository, demoDataSeeder);
    }

    // CA-5-01, CA-5-02: everything is deleted, children before the rows they point to, then the initial data set is recreated
    @Test
    void theResetDeletesEveryTableInForeignKeyOrderThenSeedsTheInitialData() {
        service(true).reset();

        InOrder order = inOrder(bookingRepository, recurrenceRepository, roomRepository, equipmentRepository,
                employeeRepository, departmentRepository, demoDataSeeder);
        order.verify(bookingRepository).deleteAll();
        order.verify(recurrenceRepository).deleteAll();
        order.verify(roomRepository).deleteAll();
        order.verify(equipmentRepository).deleteAll();
        order.verify(employeeRepository).deleteAll();
        order.verify(departmentRepository).deleteAll();
        order.verify(departmentRepository).flush();
        order.verify(demoDataSeeder).seed();
        order.verifyNoMoreInteractions();
    }

    // CA-5-04: outside the demo mode, the reset never touches the data
    @Test
    void outsideTheDemoModeTheResetTouchesNothing() {
        service(false).reset();

        verifyNoInteractions(bookingRepository, recurrenceRepository, roomRepository, equipmentRepository,
                employeeRepository, departmentRepository, demoDataSeeder);
    }
}
