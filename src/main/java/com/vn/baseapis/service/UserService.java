package com.vn.baseapis.service;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.constants.AuthoritiesConstants;
import com.vn.baseapis.constants.CommonStatus;
import com.vn.baseapis.domain.User;
import com.vn.baseapis.dto.projection.UserProjection;
import com.vn.baseapis.dto.request.CreateUserRequestDTO;
import com.vn.baseapis.dto.request.UpdateUserRequestDTO;
import com.vn.baseapis.dto.response.CommonResponseDTO;
import com.vn.baseapis.dto.response.DateRangeResponseDTO;
import com.vn.baseapis.dto.response.UserResponseDTO;
import com.vn.baseapis.exception.BusinessException;
import com.vn.baseapis.repository.UserRepository;
import com.vn.baseapis.utils.DateTimeUtils;
import com.vn.baseapis.utils.ResponseUtils;
import com.vn.baseapis.utils.ValueUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public UserResponseDTO create(CreateUserRequestDTO request) {
        User user = toEntity(request);
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    public CommonResponseDTO update(Long id, UpdateUserRequestDTO requestDTO) {
        User user = findById(id);

        user.setEmail(requestDTO.email());
        user.setFullName(requestDTO.fullName());
        user.setRole(Objects.requireNonNull(AuthoritiesConstants.find(requestDTO.role())).getValue());
        user.setStatus(Objects.requireNonNull(CommonStatus.find(requestDTO.status())).getValue());

        userRepository.save(user);

        return ResponseUtils.success();
    }

    public CommonResponseDTO delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Người dùng không tồn tại");
        }
        userRepository.deleteById(id);
        return ResponseUtils.success();
    }

    public Page<UserResponseDTO> getAllUser(Pageable pageable, String keyword, String status, String role, String dateFrom, String dateTo) {
        CommonStatus commonStatus = CommonStatus.find(status);
        Integer statusValue = ValueUtils.getOrNull(commonStatus, CommonStatus::getValue);
        AuthoritiesConstants authority = AuthoritiesConstants.find(role);
        Integer authorityValue = ValueUtils.getOrNull(authority, AuthoritiesConstants::getValue);
        DateRangeResponseDTO range = DateTimeUtils.toDateRange(dateFrom, dateTo);
        return userRepository.searchUser(keyword, statusValue, authorityValue, range.dateFrom(), range.dateTo(), pageable)
                .map(this::convertToDTO);
    }


    public CommonResponseDTO toggleStatus(Long id) {
        User user = findById(id);
        Integer newStatus = user.getStatus() == CommonStatus.ACTIVE.getValue()
                ? CommonStatus.INACTIVE.getValue()
                : CommonStatus.ACTIVE.getValue();
        user.setStatus(newStatus);
        userRepository.save(user);
        return ResponseUtils.success();
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Người dùng không tồn tại"));
    }

    private UserResponseDTO convertToDTO(UserProjection user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(AuthoritiesConstants.find(user.getRole()).name())
                .status(CommonStatus.find(user.getStatus()).name())
                .build();
    }

    private UserResponseDTO convertToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(AuthoritiesConstants.find(user.getRole()).name())
                .status(CommonStatus.find(user.getStatus()).name())
                .build();
    }

    private User toEntity(CreateUserRequestDTO request) {
        return User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .role(Objects.requireNonNull(AuthoritiesConstants.find(request.role())).getValue())
                .status(Objects.requireNonNull(CommonStatus.find(request.status())).getValue())
                .build();
    }
}
