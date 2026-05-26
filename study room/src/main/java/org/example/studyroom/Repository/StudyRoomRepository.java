package org.example.studyroom.Repository;

import org.example.studyroom.entity.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {
    List<StudyRoom> findByStatus(StudyRoom.StudyRoomStatus status);
    List<StudyRoom> findByNameContaining(String name);
}
