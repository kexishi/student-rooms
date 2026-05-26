package org.example.studyroom.service;

import org.example.studyroom.dto.SeatDTO;
import org.example.studyroom.entity.Seat;
import org.example.studyroom.entity.StudyRoom;
import org.example.studyroom.Repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatService {
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private StudyRoomService studyRoomService;
    
    public SeatDTO createSeat(SeatDTO dto) {
        StudyRoom studyRoom = studyRoomService.getStudyRoomEntityById(dto.getStudyRoomId());
        
        Seat seat = new Seat();
        seat.setSeatNumber(dto.getSeatNumber());
        seat.setStudyRoom(studyRoom);
        seat.setDescription(dto.getDescription());
        seat.setHasSocket(dto.getHasSocket() != null ? dto.getHasSocket() : false);
        
        if (dto.getStatus() != null) {
            seat.setStatus(Seat.SeatStatus.valueOf(dto.getStatus()));
        }
        
        Seat savedSeat = seatRepository.save(seat);
        return convertToDTO(savedSeat);
    }
    
    public SeatDTO updateSeat(Long id, SeatDTO dto) {
        Seat seat = seatRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("座位不存在"));
        
        seat.setSeatNumber(dto.getSeatNumber());
        seat.setDescription(dto.getDescription());
        
        if (dto.getHasSocket() != null) {
            seat.setHasSocket(dto.getHasSocket());
        }
        
        if (dto.getStatus() != null) {
            seat.setStatus(Seat.SeatStatus.valueOf(dto.getStatus()));
        }
        
        Seat savedSeat = seatRepository.save(seat);
        return convertToDTO(savedSeat);
    }
    
    public void deleteSeat(Long id) {
        if (!seatRepository.existsById(id)) {
            throw new RuntimeException("座位不存在");
        }
        seatRepository.deleteById(id);
    }
    
    public SeatDTO getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("座位不存在"));
        return convertToDTO(seat);
    }
    
    public List<SeatDTO> getSeatsByStudyRoom(Long studyRoomId) {
        return seatRepository.findByStudyRoomId(studyRoomId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<SeatDTO> getAvailableSeatsByStudyRoom(Long studyRoomId) {
        StudyRoom studyRoom = studyRoomService.getStudyRoomEntityById(studyRoomId);
        return seatRepository.findByStudyRoomAndStatus(studyRoom, Seat.SeatStatus.AVAILABLE)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public Seat getSeatEntityById(Long id) {
        return seatRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("座位不存在"));
    }
    
    private SeatDTO convertToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setStudyRoomId(seat.getStudyRoom().getId());
        dto.setStatus(seat.getStatus().name());
        dto.setDescription(seat.getDescription());
        dto.setHasSocket(seat.getHasSocket());
        return dto;
    }
}
