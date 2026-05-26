package org.example.studyroom.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String role;
}
