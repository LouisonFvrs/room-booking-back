package fr.louison.gestionsallespringapi.booking.repository;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
}
