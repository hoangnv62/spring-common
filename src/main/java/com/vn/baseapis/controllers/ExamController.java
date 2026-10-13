package com.vn.baseapis.controllers;

import com.vn.baseapis.dto.request.ExamRequestDTO;
import com.vn.baseapis.dto.response.CommonResponseDTO;
import com.vn.baseapis.dto.response.ExamResponseDTO;
import com.vn.baseapis.service.ExamService;
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
@RequestMapping("/exams")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamController {
    ExamService examService;

    @GetMapping
    public ResponseEntity<Page<ExamResponseDTO>> getAll(
            @PageableDefault Pageable pageable,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status
    ) {
        log.info("Request to search exam with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ExamResponseDTO> response = examService.findAll(keyword, status, pageable);
        log.info("Get list exam successfully with totalPage: {}, totalElement: {}", response.getTotalPages(), response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponseDTO> getById(@PathVariable Long id) {
        log.info("Request to get exam with id: {}", id);
        return ResponseEntity.ok(examService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ExamResponseDTO> create(@Valid @RequestBody ExamRequestDTO request) {
        log.info("Request to create exam");
        ExamResponseDTO response = examService.create(request);
        log.info("Create exam successfully with new id: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ExamRequestDTO request) {
        log.info("Request to update exam with id: {}", id);
        ExamResponseDTO response = examService.update(id, request);
        log.info("Update exam successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponseDTO> delete(@PathVariable Long id) {
        log.info("Request to delete exam with id: {}", id);
        CommonResponseDTO response = examService.delete(id);
        log.info("Delete exam successfully");
        return ResponseEntity.ok(response);
    }
}
