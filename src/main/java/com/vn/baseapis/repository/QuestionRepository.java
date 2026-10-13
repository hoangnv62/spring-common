package com.vn.baseapis.repository;

import com.vn.baseapis.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
                SELECT q
                FROM Question q
                WHERE q.examId = :examId AND
                      (:status IS NULL OR q.status = :status) AND
                      (:keyword IS NULL OR :keyword = '' OR q.content LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<Question> searchByCriterial(@Param("examId") Long examId, @Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);
}
