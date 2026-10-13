package com.vn.baseapis.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "exams")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exam extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "duration_in_minutes", nullable = false)
    private Integer durationInMinutes;

    @Column(name = "status", nullable = false)
    private Integer status;
}
