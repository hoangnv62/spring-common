package com.vn.baseapis.service;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.constants.CommonStatus;
import com.vn.baseapis.domain.Answer;
import com.vn.baseapis.domain.Question;
import com.vn.baseapis.dto.request.AnswerRequestDTO;
import com.vn.baseapis.dto.request.QuestionRequestDTO;
import com.vn.baseapis.dto.response.AnswerResponseDTO;
import com.vn.baseapis.dto.response.QuestionResponseDTO;
import com.vn.baseapis.exception.BusinessException;
import com.vn.baseapis.repository.AnswerRepository;
import com.vn.baseapis.repository.QuestionRepository;
import com.vn.baseapis.utils.ValueUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionService {
    QuestionRepository questionRepository;
    ExamService examService;
    AnswerRepository answerRepository;

    public Page<QuestionResponseDTO> findAllByExamId(Long examId, String keyword, String status, Pageable pageable) {
        CommonStatus statusEnum = CommonStatus.find(status);
        Integer statusValue = ValueUtils.getOrNull(statusEnum, CommonStatus::getValue);
        Page<Question> questions = questionRepository.searchByCriterial(examId, keyword, statusValue, pageable);
        List<Long> questionIds = questions.getContent().stream().map(Question::getId).toList();
        List<Answer> answers = answerRepository.findByQuestionIdInAndStatusNot(questionIds, CommonStatus.DELETE.getValue());
        Map<Long, List<Answer>> answerMap = answers.stream().collect(Collectors.groupingBy(Answer::getQuestionId));
        return questions.map(question -> buildQuestionResponseDTO(question, answerMap.getOrDefault(question.getId(), List.of())));
    }

    private Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Câu hỏi không tồn tại"));
    }

    public QuestionResponseDTO create(Long examId, QuestionRequestDTO request) {
        examService.assertExistsExam(examId);

        Question saved = questionRepository.save(buildEntity(examId, request));
        List<Answer> answers = request.answers().stream()
                .map(this::toAnswerEntity)
                .toList();
        List<Answer> savedAnswers = saveAnswers(saved.getId(), answers);
        return buildQuestionResponseDTO(saved, savedAnswers);
    }

    public QuestionResponseDTO update(Long examId, Long questionId, QuestionRequestDTO request) {
        //validate
        examService.assertExistsExam(examId);
        List<Answer> answers = request.answers().stream()
                .map(this::toAnswerEntity)
                .toList();
        Question question = findById(questionId);
        if (!Objects.equals(question.getExamId(), examId)) {
            throw new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Câu hỏi không thuộc bài thi");
        }

        //save question
        question.setContent(request.content());
        question.setStatus(Objects.requireNonNull(CommonStatus.find(request.status())).getValue());
        questionRepository.save(question);

        //save answers
        List<Answer> savedAnswers = saveAnswers(questionId, answers);
        return buildQuestionResponseDTO(question, savedAnswers);
    }

    private Question buildEntity(Long examId, QuestionRequestDTO request) {
        return Question.builder()
                .examId(examId)
                .content(request.content())
                .status(Objects.requireNonNull(CommonStatus.find(request.status())).getValue())
                .build();
    }

    private QuestionResponseDTO buildQuestionResponseDTO(Question question, List<Answer> answers) {
        List<AnswerResponseDTO> answerResponseDTOs = answers.stream()
                .map(this::toAnswerResponseDTO)
                .toList();
        return QuestionResponseDTO.builder()
                .id(question.getId())
                .content(question.getContent())
                .status(CommonStatus.find(question.getStatus()).name())
                .answers(answerResponseDTOs)
                .build();
    }

    private AnswerResponseDTO toAnswerResponseDTO(Answer answer) {
        return AnswerResponseDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .isCorrected(answer.isCorrected())
                .build();
    }

    private Answer toAnswerEntity(AnswerRequestDTO answer) {
        return Answer.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .isCorrected(answer.isCorrected())
                .status(CommonStatus.ACTIVE.getValue())
                .build();
    }

    /**
     * Đồng bộ danh sách câu trả lời của câu hỏi theo request:
     * có id -> cập nhật bản ghi đang có, không id -> thêm mới,
     * bản ghi cũ không còn trong request -> xoá mềm (status = DELETE).
     *
     * @return các câu trả lời còn hiệu lực, theo đúng thứ tự trong request
     */
    private List<Answer> saveAnswers(Long questionId, List<Answer> answers) {
        Map<Long, Answer> existing = answerRepository.findByQuestionIdAndStatusNot(questionId, CommonStatus.DELETE.getValue()).stream()
                .collect(Collectors.toMap(Answer::getId, a -> a));

        List<Answer> result = new ArrayList<>();
        Set<Long> keptIds = new HashSet<>();
        for (Answer answer : answers) {
            if (answer.getId() == null) {
                answer.setQuestionId(questionId);
                answer.setStatus(CommonStatus.ACTIVE.getValue());
                result.add(answer);
                continue;
            }
            Answer current = existing.get(answer.getId());
            if (current == null) {
                throw new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Có câu trả lời không thuộc câu hỏi");
            }
            current.setContent(answer.getContent());
            current.setCorrected(answer.isCorrected());
            current.setStatus(CommonStatus.ACTIVE.getValue());
            keptIds.add(current.getId());
            result.add(current);
        }

        List<Answer> removed = existing.values().stream()
                .filter(a -> !keptIds.contains(a.getId()))
                .toList();
        removed.forEach(a -> a.setStatus(CommonStatus.DELETE.getValue()));

        List<Answer> toSave = new ArrayList<>(result);
        toSave.addAll(removed);
        answerRepository.saveAll(toSave);
        return result;
    }
}
