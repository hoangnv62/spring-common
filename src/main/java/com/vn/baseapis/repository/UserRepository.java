package com.vn.baseapis.repository;

import com.vn.baseapis.domain.User;
import com.vn.baseapis.dto.projection.UserProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
                SELECT u.id AS id,
                       u.email AS email,
                       u.fullName AS fullName,
                       u.role AS role,
                       u.status AS status
                FROM User u
                WHERE (:keyword IS NULL OR :keyword = '' OR u.email LIKE CONCAT('%', :keyword, '%') OR u.fullName LIKE CONCAT('%', :keyword, '%')) AND
                      (:status IS NULL OR u.status = :status)
            """)
    Page<UserProjection> searchUser(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);
    Optional<User> findByEmailAndStatus(String email, Integer status);
}
