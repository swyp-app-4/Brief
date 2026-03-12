package com.swap.news.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "summarized_news",
    indexes = {
        @Index(name = "idx_summarized_news_category",   columnList = "category_id"),
        @Index(name = "idx_summarized_news_published",  columnList = "published_date DESC"),
        @Index(name = "idx_summarized_news_view",       columnList = "view_count DESC"),
        @Index(name = "idx_summarized_news_summarized", columnList = "is_summarized")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SummarizedNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sn_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id",
            foreignKey = @ForeignKey(name = "fk_sn_topic"))
    private Topic topic;

    @Column(nullable = false, length = 200)
    private String title;

    // 3줄 요약
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    // 8~10줄 본문 요약
    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "source_count")
    private int sourceCount = 0;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "save_count")
    private int saveCount = 0;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private String embedding;

    @Column(name = "is_summarized")
    private boolean isSummarized = false;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public SummarizedNews(Category category, Topic topic, String title,
                          String summary, String body,
                          int sourceCount, String thumbnailUrl, LocalDate publishedDate) {
        this.category = category;
        this.topic = topic;
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.sourceCount = sourceCount;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedDate = publishedDate;
        this.isSummarized = true;  // gemini로 항상 요약
    }

    // 임베딩
    public void updateEmbedding(String embedding) {
        this.embedding = embedding;
    }
}
