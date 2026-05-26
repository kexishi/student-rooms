package org.example.studyroom.controller;

import org.example.studyroom.dto.ApiResponse;
import org.example.studyroom.dto.SeatDTO;
import org.example.studyroom.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    @PostMapping
    public ApiResponse<SeatDTO> createSeat(@Validated @RequestBody SeatDTO dto) {
        SeatDTO seat = seatService.createSeat(dto);
        return ApiResponse.success("创建成功", seat);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<SeatDTO> updateSeat(@PathVariable Long id, 
                                           @Validated @RequestBody SeatDTO dto) {
        SeatDTO seat = seatService.updateSeat(id, dto);
        return ApiResponse.success("更新成功", seat);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<SeatDTO> getSeatById(@PathVariable Long id) {
        SeatDTO seat = seatService.getSeatById(id);
        return ApiResponse.success(seat);
    }
    
    @GetMapping("/study-room/{studyRoomId}")
    public ApiResponse<List<SeatDTO>> getSeatsByStudyRoom(@PathVariable Long studyRoomId) {
        List<SeatDTO> seats = seatService.getSeatsByStudyRoom(studyRoomId);
        return ApiResponse.success(seats);
    }
    
    @GetMapping("/study-room/{studyRoomId}/available")
    public ApiResponse<List<SeatDTO>> getAvailableSeatsByStudyRoom(@PathVariable Long studyRoomId) {
        List<SeatDTO> seats = seatService.getAvailableSeatsByStudyRoom(studyRoomId);
        return ApiResponse.success(seats);
    }
}
