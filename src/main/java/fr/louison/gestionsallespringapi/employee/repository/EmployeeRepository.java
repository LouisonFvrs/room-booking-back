package fr.louison.gestionsallespringapi.employee.repository;

import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    public List<EmployeeEntity> findAll();

    public EmployeeEntity findByEmail(String email);
}
