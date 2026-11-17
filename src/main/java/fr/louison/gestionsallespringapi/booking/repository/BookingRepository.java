package fr.louison.gestionsallespringapi.booking.repository;

import fr.louison.gestionsallespringapi.booking.model.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    public List<BookingEntity> findAllByEmployeeOrganizer_IdEmployee(Long id);

    List<BookingEntity> findAllByRecurrence_IdRecurrenceOrderByStartDateAsc(Long idRecurrence);

    boolean existsByRecurrence_IdRecurrence(Long idRecurrence);

    boolean existsByRoom_IdRoom(Long idRoom);

    @Query("""
        SELECT b
        FROM booking b
        WHERE b.status != 'CANCELLED'
          AND b.startDate < :end
          AND b.endDate > :start
        ORDER BY b.startDate
    """)
    List<BookingEntity> findActiveBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(b) > 0 
        FROM booking b 
        WHERE b.room.idRoom = :roomId 
          AND b.status != 'CANCELLED' 
          AND b.startDate < :endDate 
          AND b.endDate > :startDate
    """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COUNT(b) > 0 
        FROM booking b 
        WHERE b.room.idRoom = :roomId 
          AND b.idBooking != :currentBookingId
          AND b.status != 'CANCELLED' 
          AND b.startDate < :endDate 
          AND b.endDate > :startDate
    """)
    boolean existsOverlappingBookingExcludingCurrent(
            @Param("roomId") Long roomId,
            @Param("currentBookingId") Long currentBookingId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
