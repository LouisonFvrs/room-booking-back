package fr.louison.gestionsallespringapi.invitation.repository;

import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    Page<InvitationEntity> findByBooking_IdBooking(Long idBooking, Pageable pageable);

    Page<InvitationEntity> findByEmployeeAttendee_IdEmployee(Long idEmployee, Pageable pageable);
}
