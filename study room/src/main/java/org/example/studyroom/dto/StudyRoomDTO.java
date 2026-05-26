package org.example.studyroom.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class StudyRoomDTO {
    private Long id;
    
    @NotBlank(message = "自习室名称不能为空")
    private String name;
    
    private String location;
    
    private String description;
    
    @NotNull(message = "总座位数不能为空")
    @Min(value = 1, message = "总座位数至少为1")
    private Integer totalSeats;
    
    @NotNull(message = "开放时间不能为空")
    private LocalTime openTime;
    
    @NotNull(message = "关闭时间不能为空")
    private LocalTime closeTime;
    
    private String status;
}
