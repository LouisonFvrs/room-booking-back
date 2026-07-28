package fr.louison.gestionsallespringapi.invitation.repository;

import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    public Page<InvitationEntity> findByBooking(Pageable pageable, Long id);
}
