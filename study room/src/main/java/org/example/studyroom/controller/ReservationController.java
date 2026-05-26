package org.example.studyroom.controller;

import org.example.studyroom.dto.ApiResponse;
import org.example.studyroom.dto.ReservationCreateDTO;
import org.example.studyroom.dto.ReservationResponseDTO;
import org.example.studyroom.entity.Reservation;
import org.example.studyroom.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    @PostMapping("/user/{userId}")
    public ApiResponse<ReservationResponseDTO> createReservation(
            @PathVariable Long userId,
            @Validated @RequestBody ReservationCreateDTO dto) {
        ReservationResponseDTO reservation = reservationService.createReservation(userId, dto);
        return ApiResponse.success("预约成功", reservation);
    }
    
    @DeleteMapping("/user/{userId}/reservation/{reservationId}")
    public ApiResponse<ReservationResponseDTO> cancelReservation(
            @PathVariable Long userId,
            @PathVariable Long reservationId) {
        ReservationResponseDTO reservation = reservationService.cancelReservation(userId, reservationId);
        return ApiResponse.success("取消成功", reservation);
    }
    
    @GetMapping("/user/{userId}")
    public ApiResponse<List<ReservationResponseDTO>> getUserReservations(@PathVariable Long userId) {
        List<ReservationResponseDTO> reservations = reservationService.getUserReservations(userId);
        return ApiResponse.success(reservations);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<ReservationResponseDTO> getReservationById(@PathVariable Long id) {
        ReservationResponseDTO reservation = reservationService.getReservationById(id);
        return ApiResponse.success(reservation);
    }
    
    @GetMapping("/date/{date}")
    public ApiResponse<List<ReservationResponseDTO>> getReservationsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ReservationResponseDTO> reservations = reservationService.getReservationsByDate(date);
        return ApiResponse.success(reservations);
    }
    
    @GetMapping("/seat/{seatId}/date/{date}")
    public ApiResponse<List<ReservationResponseDTO>> getReservationsBySeat(
            @PathVariable Long seatId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ReservationResponseDTO> reservations = reservationService.getReservationsBySeat(seatId, date);
        return ApiResponse.success(reservations);
    }
    
    // 获取可用时间段
    @GetMapping("/seat/{seatId}/date/{date}/available-slots")
    public ApiResponse<List<ReservationService.TimeSlot>> getAvailableTimeSlots(
            @PathVariable Long seatId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ReservationService.TimeSlot> slots = reservationService.getAvailableTimeSlots(seatId, date);
        return ApiResponse.success(slots);
    }
    
    // ========== 管理员接口 ==========
    
    // 获取所有预约
    @GetMapping("/admin/all")
    public ApiResponse<List<ReservationResponseDTO>> getAllReservations() {
        List<ReservationResponseDTO> reservations = reservationService.getAllReservations();
        return ApiResponse.success(reservations);
    }
    
    // 根据状态查询预约
    @GetMapping("/admin/status/{status}")
    public ApiResponse<List<ReservationResponseDTO>> getReservationsByStatus(
            @PathVariable String status) {
        Reservation.ReservationStatus reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
        List<ReservationResponseDTO> reservations = reservationService.getReservationsByStatus(reservationStatus);
        return ApiResponse.success(reservations);
    }
    
    // 管理员取消预约
    @PostMapping("/admin/{reservationId}/cancel")
    public ApiResponse<ReservationResponseDTO> adminCancelReservation(@PathVariable Long reservationId) {
        ReservationResponseDTO reservation = reservationService.adminCancelReservation(reservationId);
        return ApiResponse.success("取消成功", reservation);
    }
    
    // 管理员修改预约
    @PutMapping("/admin/{reservationId}")
    public ApiResponse<ReservationResponseDTO> adminUpdateReservation(
            @PathVariable Long reservationId,
            @Validated @RequestBody ReservationCreateDTO dto) {
        ReservationResponseDTO reservation = reservationService.adminUpdateReservation(reservationId, dto);
        return ApiResponse.success("修改成功", reservation);
    }
    
    // 管理员删除预约
    @DeleteMapping("/admin/{reservationId}")
    public ApiResponse<Void> adminDeleteReservation(@PathVariable Long reservationId) {
        reservationService.adminDeleteReservation(reservationId);
        return ApiResponse.success("删除成功", null);
    }
}
