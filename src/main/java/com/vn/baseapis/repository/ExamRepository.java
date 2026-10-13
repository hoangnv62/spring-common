package com.vn.baseapis.repository;

import com.vn.baseapis.domain.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    /**
     * Tìm kiếm bài thi theo tên và trạng thái, luôn bỏ qua bài thi đã xoá mềm.
     */
    @Query("""
                SELECT e
                FROM Exam e
                WHERE e.status <> :deletedStatus AND
                      (:status IS NULL OR e.status = :status) AND
                      (:keyword IS NULL OR :keyword = '' OR e.name LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<Exam> searchByCriterial(@Param("keyword") String keyword, @Param("status") Integer status,
                                 @Param("deletedStatus") Integer deletedStatus, Pageable pageable);

    Optional<Exam> findByIdAndStatusNot(Long id, Integer status);
}
