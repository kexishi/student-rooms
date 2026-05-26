package org.example.studyroom.controller;

import org.example.studyroom.dto.ApiResponse;
import org.example.studyroom.dto.StudyRoomDTO;
import org.example.studyroom.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/study-rooms")
public class StudyRoomController {
    
    @Autowired
    private StudyRoomService studyRoomService;
    
    @PostMapping("/user/{userId}")
    public ApiResponse<StudyRoomDTO> createStudyRoom(@PathVariable Long userId,
                                                     @Validated @RequestBody StudyRoomDTO dto) {
        StudyRoomDTO studyRoom = studyRoomService.createStudyRoom(userId, dto);
        return ApiResponse.success("创建成功", studyRoom);
    }
    
    @PutMapping("/user/{userId}/{id}")
    public ApiResponse<StudyRoomDTO> updateStudyRoom(@PathVariable Long userId,
                                                     @PathVariable Long id, 
                                                      @Validated @RequestBody StudyRoomDTO dto) {
        StudyRoomDTO studyRoom = studyRoomService.updateStudyRoom(userId, id, dto);
        return ApiResponse.success("更新成功", studyRoom);
    }
    
    @DeleteMapping("/user/{userId}/{id}")
    public ApiResponse<Void> deleteStudyRoom(@PathVariable Long userId, @PathVariable Long id) {
        studyRoomService.deleteStudyRoom(userId, id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<StudyRoomDTO> getStudyRoomById(@PathVariable Long id) {
        StudyRoomDTO studyRoom = studyRoomService.getStudyRoomById(id);
        return ApiResponse.success(studyRoom);
    }
    
    @GetMapping
    public ApiResponse<List<StudyRoomDTO>> getAllStudyRooms() {
        List<StudyRoomDTO> studyRooms = studyRoomService.getAllStudyRooms();
        return ApiResponse.success(studyRooms);
    }
    
    @GetMapping("/status/{status}")
    public ApiResponse<List<StudyRoomDTO>> getStudyRoomsByStatus(@PathVariable String status) {
        List<StudyRoomDTO> studyRooms = studyRoomService.getStudyRoomsByStatus(status);
        return ApiResponse.success(studyRooms);
    }
}
