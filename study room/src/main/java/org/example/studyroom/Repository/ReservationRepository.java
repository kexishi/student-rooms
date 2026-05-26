package org.example.studyroom.Repository;

import org.example.studyroom.entity.Reservation;
import org.example.studyroom.entity.Seat;
import org.example.studyroom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByUserOrderByCreatedAtDesc(User user);
    List<Reservation> findBySeat(Seat seat);
    List<Reservation> findBySeatAndDate(Seat seat, LocalDate date);
    List<Reservation> findByDate(LocalDate date);
    
    @Query("SELECT r FROM Reservation r WHERE r.seat = :seat AND r.date = :date " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((r.startTime <= :endTime AND r.endTime >= :startTime))")
    List<Reservation> findConflictingReservations(@Param("seat") Seat seat, 
                                                   @Param("date") LocalDate date,
                                                   @Param("startTime") LocalTime startTime, 
                                                   @Param("endTime") LocalTime endTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId ORDER BY r.date DESC, r.startTime DESC")
    List<Reservation> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    // 查询所有预约，按日期和开始时间排序
    @Query("SELECT r FROM Reservation r ORDER BY r.date DESC, r.startTime DESC")
    List<Reservation> findAllOrderByDateDesc();
    
    // 根据状态查询预约
    List<Reservation> findByStatusOrderByDateDesc(Reservation.ReservationStatus status);
}
