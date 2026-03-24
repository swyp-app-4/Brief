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

    // 대분류
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_group_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_category_category_group"))
    private CategoryGroup categoryGroup;

    // 소분류
    @Column(nullable = false, length = 255, unique = true)
    private String name;

    // Naver API 검색어
    @Column(length = 255)
    private String query;

    // 저장 시 query가 null 이라면 name 저장
    @Builder
    public Category(CategoryGroup categoryGroup, String name, String query) {
        this.categoryGroup = categoryGroup;
        this.name = name;
        this.query = (query != null) ? query : name;
    }
}
