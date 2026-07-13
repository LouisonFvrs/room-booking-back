package fr.louison.gestionsallespringapi.invitation.repository;

import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
}
