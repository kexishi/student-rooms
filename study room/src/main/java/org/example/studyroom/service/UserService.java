package org.example.studyroom.service;

import org.example.studyroom.dto.UserRegisterDTO;
import org.example.studyroom.dto.UserResponseDTO;
import org.example.studyroom.entity.User;
import org.example.studyroom.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public UserResponseDTO register(UserRegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        if (dto.getPhone() != null && userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        
        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }
    
    public UserResponseDTO registerAdmin(UserRegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(User.UserRole.ADMIN);
        
        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }
    
    public boolean isAdmin(Long userId) {
        User user = getUserEntityById(userId);
        return user.getRole() == User.UserRole.ADMIN;
    }
    
    public UserResponseDTO login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        
        return convertToResponseDTO(user);
    }
    
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToResponseDTO(user);
    }
    
    public UserResponseDTO updateUser(Long id, UserRegisterDTO dto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!user.getUsername().equals(dto.getUsername()) && 
            userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        
        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }
    
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
