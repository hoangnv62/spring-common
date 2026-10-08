package com.vn.baseapis.controllers;

import com.vn.baseapis.dto.request.CreateUserRequestDTO;
import com.vn.baseapis.dto.request.UpdateUserRequestDTO;
import com.vn.baseapis.dto.response.CommonResponseDTO;
import com.vn.baseapis.dto.response.UserExportResponseDTO;
import com.vn.baseapis.dto.response.UserResponseDTO;
import com.vn.baseapis.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAll(
            @PageableDefault Pageable pageable,
            @RequestParam(name = "dateFrom", required = false) String dateFrom,
            @RequestParam(name = "dateTo", required = false) String dateTo,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "role", required = false) String role
    ) {
        log.info("Request to search user with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponseDTO> response = userService.getAllUser(pageable, keyword, status, role, dateFrom, dateTo);
        log.info("Get list user successfully with totalPage: {}, totalElement: {}", response.getTotalPages(), response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody CreateUserRequestDTO request) {
        log.info("Request to create user");
        UserResponseDTO newUser = userService.create(request);
        log.info("Create user successfully with new id: {}", newUser.getId());
        return ResponseEntity.ok(newUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponseDTO> update(@PathVariable Long id, @Valid UpdateUserRequestDTO request) {
        log.info("Request to update user with id: {}", id);
        CommonResponseDTO response = userService.update(id, request);
        log.info("Update user successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponseDTO> delete(@PathVariable Long id) {
        log.info("Request to delete user with id: {}", id);
        CommonResponseDTO response = userService.delete(id);
        log.info("Delete user successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResponseDTO> toggle(@PathVariable Long id) {
        log.info("Request to toggle status with id: {}", id);
        CommonResponseDTO response = userService.toggleStatus(id);
        log.info("Change status successfully");
        return ResponseEntity.ok(response);
    }
}
