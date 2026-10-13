package com.vn.baseapis.controllers;

import com.vn.baseapis.dto.request.QuestionRequestDTO;
import com.vn.baseapis.dto.response.QuestionResponseDTO;
import com.vn.baseapis.service.QuestionService;
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
@RequestMapping("/exams/{examId}/questions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionController {
    QuestionService questionService;

    @GetMapping
    public ResponseEntity<Page<QuestionResponseDTO>> getAll(
            @PathVariable Long examId,
            @PageableDefault Pageable pageable,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status
    ) {
        log.info("Request to search question of exam {} with page: {}, size: {}", examId, pageable.getPageNumber(), pageable.getPageSize());
        Page<QuestionResponseDTO> response = questionService.findAllByExamId(examId, keyword, status, pageable);
        log.info("Get list question successfully with totalPage: {}, totalElement: {}", response.getTotalPages(), response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<QuestionResponseDTO> create(@PathVariable Long examId,
                                                      @Valid @RequestBody QuestionRequestDTO request) {
        log.info("Request to create question for exam {}", examId);
        QuestionResponseDTO response = questionService.create(examId, request);
        log.info("Create question successfully with new id: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponseDTO> update(@PathVariable Long examId,
                                                      @PathVariable Long questionId,
                                                      @Valid @RequestBody QuestionRequestDTO request) {
        log.info("Request to update question {} of exam {}", questionId, examId);
        QuestionResponseDTO response = questionService.update(examId, questionId, request);
        log.info("Update question successfully");
        return ResponseEntity.ok(response);
    }
}
