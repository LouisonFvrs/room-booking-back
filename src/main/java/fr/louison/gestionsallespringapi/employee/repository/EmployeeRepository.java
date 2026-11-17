package fr.louison.gestionsallespringapi.employee.repository;

import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    public List<EmployeeEntity> findAll();

    public Optional<EmployeeEntity> findByEmail(String email);

    boolean existsByDepartment_IdDepartment(Long idDepartment);

    boolean existsByEmail(@NotBlank(message = "L'email ne peut pas être vide") @Email(message = "L'email doit avoir un format valide") String email);
}
