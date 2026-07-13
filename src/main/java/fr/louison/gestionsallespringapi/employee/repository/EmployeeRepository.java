package fr.louison.gestionsallespringapi.employee.repository;

import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
}
