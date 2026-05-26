package org.example.studyroom.Repository;

import org.example.studyroom.entity.Seat;
import org.example.studyroom.entity.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByStudyRoom(StudyRoom studyRoom);
    List<Seat> findByStudyRoomAndStatus(StudyRoom studyRoom, Seat.SeatStatus status);
    List<Seat> findByStudyRoomId(Long studyRoomId);
    List<Seat> findByStatus(Seat.SeatStatus status);
}
