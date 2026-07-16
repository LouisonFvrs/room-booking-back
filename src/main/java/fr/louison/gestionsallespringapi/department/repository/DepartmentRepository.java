package fr.louison.gestionsallespringapi.department.repository;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {
}
