package org.example.studyroom.service;

import org.example.studyroom.dto.ReservationCreateDTO;
import org.example.studyroom.dto.ReservationResponseDTO;
import org.example.studyroom.entity.Reservation;
import org.example.studyroom.entity.Seat;
import org.example.studyroom.entity.StudyRoom;
import org.example.studyroom.entity.User;
import org.example.studyroom.Repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SeatService seatService;
    
    // 时间段时长（小时）
    private static final int SLOT_DURATION_HOURS = 1;
    
    public ReservationResponseDTO createReservation(Long userId, ReservationCreateDTO dto) {
        User user = userService.getUserEntityById(userId);
        Seat seat = seatService.getSeatEntityById(dto.getSeatId());
        StudyRoom studyRoom = seat.getStudyRoom();
        
        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new RuntimeException("该座位不可用");
        }
        
        // 获取时间段
        TimeSlot slot = getTimeSlot(studyRoom, dto.getTimeSlot());
        if (slot == null) {
            throw new RuntimeException("无效的时间段");
        }
        
        // 检查是否已被预约
        List<Reservation> existingReservations = reservationRepository.findBySeatAndDate(seat, dto.getDate());
        List<Reservation> activeReservations = existingReservations.stream()
            .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
            .collect(Collectors.toList());
        
        for (Reservation existing : activeReservations) {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), 
                            existing.getStartTime(), existing.getEndTime())) {
                throw new RuntimeException("该时间段已被预约");
            }
        }
        
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setSeat(seat);
        reservation.setStudyRoom(studyRoom);
        reservation.setDate(dto.getDate());
        reservation.setStartTime(slot.getStartTime());
        reservation.setEndTime(slot.getEndTime());
        reservation.setRemark(dto.getRemark());
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        
        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToResponseDTO(savedReservation);
    }
    
    public ReservationResponseDTO cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("预约记录不存在"));
        
        if (!reservation.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权取消此预约");
        }
        
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new RuntimeException("该预约已取消");
        }
        
        if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            throw new RuntimeException("该预约已完成，无法取消");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToResponseDTO(savedReservation);
    }
    
    public List<ReservationResponseDTO> getUserReservations(Long userId) {
        User user = userService.getUserEntityById(userId);
        return reservationRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }
    
    public ReservationResponseDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("预约记录不存在"));
        return convertToResponseDTO(reservation);
    }
    
    public List<ReservationResponseDTO> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByDate(date).stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }
    
    public List<ReservationResponseDTO> getReservationsBySeat(Long seatId, LocalDate date) {
        Seat seat = seatService.getSeatEntityById(seatId);
        List<Reservation> reservations = reservationRepository.findBySeatAndDate(seat, date);
        return reservations.stream()
            .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }
    
    // 获取可用时间段
    public List<TimeSlot> getAvailableTimeSlots(Long seatId, LocalDate date) {
        Seat seat = seatService.getSeatEntityById(seatId);
        StudyRoom studyRoom = seat.getStudyRoom();
        List<TimeSlot> allSlots = generateTimeSlots(studyRoom);
        
        List<Reservation> existingReservations = reservationRepository.findBySeatAndDate(seat, date);
        List<Reservation> activeReservations = existingReservations.stream()
            .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
            .collect(Collectors.toList());
        
        return allSlots.stream()
            .filter(slot -> !isSlotOccupied(slot, activeReservations))
            .collect(Collectors.toList());
    }
    
    // ========== 管理员功能 ==========
    
    // 获取所有预约（管理员）
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAllOrderByDateDesc().stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }
    
    // 根据状态查询预约（管理员）
    public List<ReservationResponseDTO> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatusOrderByDateDesc(status).stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }
    
    // 管理员取消任意预约
    public ReservationResponseDTO adminCancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("预约记录不存在"));
        
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new RuntimeException("该预约已取消");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToResponseDTO(savedReservation);
    }
    
    // 管理员修改预约
    public ReservationResponseDTO adminUpdateReservation(Long reservationId, ReservationCreateDTO dto) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("预约记录不存在"));
        
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new RuntimeException("已取消的预约无法修改");
        }
        
        Seat seat = seatService.getSeatEntityById(dto.getSeatId());
        StudyRoom studyRoom = seat.getStudyRoom();
        
        // 获取时间段
        TimeSlot slot = getTimeSlot(studyRoom, dto.getTimeSlot());
        if (slot == null) {
            throw new RuntimeException("无效的时间段");
        }
        
        // 检查是否与其他预约冲突（排除当前预约）
        List<Reservation> existingReservations = reservationRepository.findBySeatAndDate(seat, dto.getDate());
        List<Reservation> activeReservations = existingReservations.stream()
            .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
            .filter(r -> !r.getId().equals(reservationId)) // 排除当前预约
            .collect(Collectors.toList());
        
        for (Reservation existing : activeReservations) {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), 
                            existing.getStartTime(), existing.getEndTime())) {
                throw new RuntimeException("该时间段已被预约");
            }
        }
        
        // 更新预约信息
        reservation.setSeat(seat);
        reservation.setStudyRoom(studyRoom);
        reservation.setDate(dto.getDate());
        reservation.setStartTime(slot.getStartTime());
        reservation.setEndTime(slot.getEndTime());
        reservation.setRemark(dto.getRemark());
        
        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToResponseDTO(savedReservation);
    }
    
    // 管理员删除预约（物理删除）
    public void adminDeleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("预约记录不存在"));
        reservationRepository.delete(reservation);
    }
    
    private boolean isSlotOccupied(TimeSlot slot, List<Reservation> reservations) {
        for (Reservation r : reservations) {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), 
                            r.getStartTime(), r.getEndTime())) {
                return true;
            }
        }
        return false;
    }
    
    // 根据自习室开放时间生成时间段（1小时）
    public List<TimeSlot> generateTimeSlots(StudyRoom studyRoom) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime openTime = studyRoom.getOpenTime();
        LocalTime closeTime = studyRoom.getCloseTime();
        
        int index = 0;
        LocalTime currentStart = openTime;
        
        while (currentStart.plusHours(SLOT_DURATION_HOURS).isBefore(closeTime) || 
               currentStart.plusHours(SLOT_DURATION_HOURS).equals(closeTime)) {
            LocalTime currentEnd = currentStart.plusHours(SLOT_DURATION_HOURS);
            String label = String.format("%02d:%02d-%02d:%02d", 
                currentStart.getHour(), currentStart.getMinute(),
                currentEnd.getHour(), currentEnd.getMinute());
            slots.add(new TimeSlot(index, currentStart, currentEnd, label));
            index++;
            currentStart = currentEnd;
        }
        
        return slots;
    }
    
    // 根据索引获取时间段
    private TimeSlot getTimeSlot(StudyRoom studyRoom, int slotIndex) {
        List<TimeSlot> slots = generateTimeSlots(studyRoom);
        return slots.stream()
            .filter(s -> s.getIndex() == slotIndex)
            .findFirst()
            .orElse(null);
    }
    
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, 
                                  LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    private ReservationResponseDTO convertToResponseDTO(Reservation reservation) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getUser().getId());
        dto.setUserName(reservation.getUser().getName());
        dto.setSeatId(reservation.getSeat().getId());
        dto.setSeatNumber(reservation.getSeat().getSeatNumber());
        dto.setStudyRoomId(reservation.getStudyRoom().getId());
        dto.setStudyRoomName(reservation.getStudyRoom().getName());
        dto.setDate(reservation.getDate());
        dto.setStartTime(reservation.getStartTime());
        dto.setEndTime(reservation.getEndTime());
        dto.setStatus(reservation.getStatus().name());
        dto.setRemark(reservation.getRemark());
        return dto;
    }
    
    // 时间段内部类
    public static class TimeSlot {
        private int index;
        private LocalTime startTime;
        private LocalTime endTime;
        private String label;
        
        public TimeSlot(int index, LocalTime startTime, LocalTime endTime, String label) {
            this.index = index;
            this.startTime = startTime;
            this.endTime = endTime;
            this.label = label;
        }
        
        public int getIndex() { return index; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public String getLabel() { return label; }
    }
}
