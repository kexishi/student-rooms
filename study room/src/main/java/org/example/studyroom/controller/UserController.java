package org.example.studyroom.controller;

import org.example.studyroom.dto.ApiResponse;
import org.example.studyroom.dto.UserLoginDTO;
import org.example.studyroom.dto.UserRegisterDTO;
import org.example.studyroom.dto.UserResponseDTO;
import org.example.studyroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ApiResponse<UserResponseDTO> register(@Validated @RequestBody UserRegisterDTO dto) {
        UserResponseDTO user = userService.register(dto);
        return ApiResponse.success("注册成功", user);
    }
    
    @PostMapping("/register/admin")
    public ApiResponse<UserResponseDTO> registerAdmin(@Validated @RequestBody UserRegisterDTO dto) {
        UserResponseDTO user = userService.registerAdmin(dto);
        return ApiResponse.success("管理员注册成功", user);
    }
    
    @PostMapping("/login")
    public ApiResponse<UserResponseDTO> login(@Validated @RequestBody UserLoginDTO dto) {
        UserResponseDTO user = userService.login(dto.getUsername(), dto.getPassword());
        return ApiResponse.success("登录成功", user);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<UserResponseDTO> updateUser(@PathVariable Long id, 
                                                    @Validated @RequestBody UserRegisterDTO dto) {
        UserResponseDTO user = userService.updateUser(id, dto);
        return ApiResponse.success("更新成功", user);
    }
}
