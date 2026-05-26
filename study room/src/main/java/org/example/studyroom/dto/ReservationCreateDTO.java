package org.example.studyroom.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationCreateDTO {
    @NotNull(message = "座位ID不能为空")
    private Long seatId;
    
    @NotNull(message = "预约日期不能为空")
    @Future(message = "预约日期必须是未来的日期")
    private LocalDate date;
    
    @NotNull(message = "时间段不能为空")
    private Integer timeSlot;  // 0=08:00-10:00, 1=10:00-12:00, 2=14:00-16:00, 3=16:00-18:00, 4=19:00-21:00
    
    private String remark;
}
