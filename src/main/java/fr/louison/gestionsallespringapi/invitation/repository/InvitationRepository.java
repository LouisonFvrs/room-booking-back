package fr.louison.gestionsallespringapi.invitation.repository;

import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    public List<InvitationEntity> findByBooking(Long id);
}
