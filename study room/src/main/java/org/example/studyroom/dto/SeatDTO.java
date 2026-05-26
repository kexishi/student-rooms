package org.example.studyroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatDTO {
    private Long id;
    
    @NotBlank(message = "座位号不能为空")
    private String seatNumber;
    
    @NotNull(message = "自习室ID不能为空")
    private Long studyRoomId;
    
    private String status;
    
    private String description;
    
    private Boolean hasSocket;
}
