package com.brife.news.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    @Column(nullable = false, length = 255, unique = true)
    private String name;

    // Naver API 검색어
    @Column(nullable = false, length = 255)
    private String query;
}
