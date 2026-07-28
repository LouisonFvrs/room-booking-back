package fr.louison.gestionsallespringapi.department.repository;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    public Optional<DepartmentEntity> findByName(String name);

    boolean existsByName(@NotNull @NotBlank(message = "Un nom doit être fourni") String name);
}
