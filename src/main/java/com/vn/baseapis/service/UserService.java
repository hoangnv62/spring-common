package com.vn.baseapis.service;

import com.vn.baseapis.dto.response.UserResponseDTO;
import com.vn.baseapis.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    public Page<UserResponseDTO> getAllUser(Pageable pageable){
        return userRepository.findAll(pageable)
                .map(user -> new UserResponseDTO());
    }
}
