package fr.louison.gestionsallespringapi.booking.repository;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
}
