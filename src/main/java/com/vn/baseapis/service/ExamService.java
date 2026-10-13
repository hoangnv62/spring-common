package com.vn.baseapis.service;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.constants.CommonStatus;
import com.vn.baseapis.domain.Exam;
import com.vn.baseapis.dto.request.ExamRequestDTO;
import com.vn.baseapis.dto.response.CommonResponseDTO;
import com.vn.baseapis.dto.response.ExamResponseDTO;
import com.vn.baseapis.exception.BusinessException;
import com.vn.baseapis.repository.ExamRepository;
import com.vn.baseapis.utils.ResponseUtils;
import com.vn.baseapis.utils.ValueUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamService {
    ExamRepository examRepository;

    public Page<ExamResponseDTO> findAll(String keyword, String status, Pageable pageable) {
        Integer statusValue = ValueUtils.getOrNull(CommonStatus.find(status), CommonStatus::getValue);
        return examRepository.searchByCriterial(keyword, statusValue, CommonStatus.DELETE.getValue(), pageable)
                .map(this::toResponseDTO);
    }

    public ExamResponseDTO getById(Long id) {
        return toResponseDTO(findById(id));
    }

    public ExamResponseDTO create(ExamRequestDTO request) {
        Exam exam = Exam.builder()
                .name(request.name())
                .description(request.description())
                .durationInMinutes(request.durationInMinutes())
                .status(toStatusValue(request.status()))
                .build();
        return toResponseDTO(examRepository.save(exam));
    }

    public ExamResponseDTO update(Long id, ExamRequestDTO request) {
        Exam exam = findById(id);
        exam.setName(request.name());
        exam.setDescription(request.description());
        exam.setDurationInMinutes(request.durationInMinutes());
        exam.setStatus(toStatusValue(request.status()));
        return toResponseDTO(examRepository.save(exam));
    }

    /**
     * Xoá mềm (status = DELETE) để không mất dữ liệu câu hỏi/câu trả lời đang tham chiếu tới bài thi.
     */
    public CommonResponseDTO delete(Long id) {
        Exam exam = findById(id);
        exam.setStatus(CommonStatus.DELETE.getValue());
        examRepository.save(exam);
        return ResponseUtils.success();
    }

    public void assertExistsExam(Long id) {
        findById(id);
    }

    /**
     * Tìm bài thi chưa bị xoá mềm, không có thì ném ENTITY_NOT_FOUND.
     */
    private Exam findById(Long id) {
        return examRepository.findByIdAndStatusNot(id, CommonStatus.DELETE.getValue())
                .orElseThrow(() -> new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Bài thi không tồn tại"));
    }

    private Integer toStatusValue(String status) {
        return Objects.requireNonNull(CommonStatus.find(status)).getValue();
    }

    private ExamResponseDTO toResponseDTO(Exam exam) {
        return ExamResponseDTO.builder()
                .id(exam.getId())
                .name(exam.getName())
                .description(exam.getDescription())
                .durationInMinutes(exam.getDurationInMinutes())
                .status(ValueUtils.getOrNull(CommonStatus.find(exam.getStatus()), CommonStatus::name))
                .build();
    }
}
