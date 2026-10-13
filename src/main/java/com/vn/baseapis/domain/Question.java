package com.vn.baseapis.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "status", nullable = false)
    private Integer status;
}
