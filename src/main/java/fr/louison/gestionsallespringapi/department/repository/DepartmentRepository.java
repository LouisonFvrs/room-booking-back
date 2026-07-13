package fr.louison.gestionsallespringapi.department.repository;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {
}
