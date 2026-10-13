package com.vn.baseapis.repository;

import com.vn.baseapis.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdAndStatusNot(Long questionId, Integer status);

    List<Answer> findByQuestionIdInAndStatusNot(Collection<Long> questionIds, Integer status);
}
