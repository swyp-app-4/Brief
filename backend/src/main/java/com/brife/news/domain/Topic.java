package com.brife.news.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "topic",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_topic_category_keyword_date",
        columnNames = {"category_id", "keyword", "created_date"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_topic_category"))
    private Category category;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(name = "article_count")
    private int articleCount = 0;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Builder
    public Topic(Category category, String keyword, int articleCount, LocalDate createdDate) {
        this.category = category;
        this.keyword = keyword;
        this.articleCount = articleCount;
        this.createdDate = createdDate;
    }

    public void addArticleCount(int count) {
        this.articleCount += count;
    }
}
