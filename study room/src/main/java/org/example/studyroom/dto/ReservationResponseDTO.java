package org.example.studyroom.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long seatId;
    private String seatNumber;
    private Long studyRoomId;
    private String studyRoomName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String remark;
}
