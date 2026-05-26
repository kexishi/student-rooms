package org.example.studyroom.service;

import org.example.studyroom.dto.StudyRoomDTO;
import org.example.studyroom.entity.Seat;
import org.example.studyroom.entity.StudyRoom;
import org.example.studyroom.Repository.SeatRepository;
import org.example.studyroom.Repository.StudyRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyRoomService {
    
    @Autowired
    private StudyRoomRepository studyRoomRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private UserService userService;
    
    private void checkAdmin(Long userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("只有管理员可以执行此操作");
        }
    }
    
    public StudyRoomDTO createStudyRoom(Long userId, StudyRoomDTO dto) {
        checkAdmin(userId);
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setName(dto.getName());
        studyRoom.setLocation(dto.getLocation());
        studyRoom.setDescription(dto.getDescription());
        studyRoom.setTotalSeats(dto.getTotalSeats());
        studyRoom.setOpenTime(dto.getOpenTime());
        studyRoom.setCloseTime(dto.getCloseTime());
        
        if (dto.getStatus() != null) {
            studyRoom.setStatus(StudyRoom.StudyRoomStatus.valueOf(dto.getStatus()));
        }
        
        StudyRoom savedStudyRoom = studyRoomRepository.save(studyRoom);
        
        for (int i = 1; i <= dto.getTotalSeats(); i++) {
            Seat seat = new Seat();
            seat.setSeatNumber(String.valueOf(i));
            seat.setStudyRoom(savedStudyRoom);
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHasSocket(false);
            seatRepository.save(seat);
        }
        
        return convertToDTO(savedStudyRoom);
    }
    
    public StudyRoomDTO updateStudyRoom(Long userId, Long id, StudyRoomDTO dto) {
        checkAdmin(userId);
        StudyRoom studyRoom = studyRoomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("自习室不存在"));
        
        studyRoom.setName(dto.getName());
        studyRoom.setLocation(dto.getLocation());
        studyRoom.setDescription(dto.getDescription());
        studyRoom.setOpenTime(dto.getOpenTime());
        studyRoom.setCloseTime(dto.getCloseTime());
        
        if (dto.getStatus() != null) {
            studyRoom.setStatus(StudyRoom.StudyRoomStatus.valueOf(dto.getStatus()));
        }
        
        StudyRoom savedStudyRoom = studyRoomRepository.save(studyRoom);
        return convertToDTO(savedStudyRoom);
    }
    
    public void deleteStudyRoom(Long userId, Long id) {
        checkAdmin(userId);
        if (!studyRoomRepository.existsById(id)) {
            throw new RuntimeException("自习室不存在");
        }
        studyRoomRepository.deleteById(id);
    }
    
    public StudyRoomDTO getStudyRoomById(Long id) {
        StudyRoom studyRoom = studyRoomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("自习室不存在"));
        return convertToDTO(studyRoom);
    }
    
    public List<StudyRoomDTO> getAllStudyRooms() {
        return studyRoomRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<StudyRoomDTO> getStudyRoomsByStatus(String status) {
        return studyRoomRepository.findByStatus(StudyRoom.StudyRoomStatus.valueOf(status))
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public StudyRoom getStudyRoomEntityById(Long id) {
        return studyRoomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("自习室不存在"));
    }
    
    private StudyRoomDTO convertToDTO(StudyRoom studyRoom) {
        StudyRoomDTO dto = new StudyRoomDTO();
        dto.setId(studyRoom.getId());
        dto.setName(studyRoom.getName());
        dto.setLocation(studyRoom.getLocation());
        dto.setDescription(studyRoom.getDescription());
        dto.setTotalSeats(studyRoom.getTotalSeats());
        dto.setOpenTime(studyRoom.getOpenTime());
        dto.setCloseTime(studyRoom.getCloseTime());
        dto.setStatus(studyRoom.getStatus().name());
        return dto;
    }
}
