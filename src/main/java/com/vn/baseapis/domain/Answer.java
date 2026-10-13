package com.vn.baseapis.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_question", nullable = false)
    private Long questionId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_corrected", nullable = false)
    private boolean isCorrected;

    @Column(name = "status", nullable = false)
    private Integer status;
}
