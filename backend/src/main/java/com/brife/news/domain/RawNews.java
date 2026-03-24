package com.brife.news.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "raw_news",
    indexes = {
        @Index(name = "idx_raw_news_topic",      columnList = "topic_id"),
        @Index(name = "idx_raw_news_summarized", columnList = "summarized_news_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RawNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rn_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summarized_news_id",
            foreignKey = @ForeignKey(name = "fk_rn_summarized"))
    private SummarizedNews summarizedNews;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "source_url", nullable = false, length = 500)
    private String sourceUrl;

    @Column(name = "naver_url", nullable = false, length = 500, unique = true)
    private String naverUrl;

    @Column(name = "pub_date", nullable = false)
    private LocalDateTime pubDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RawNews(Category category, String title,
                   String sourceUrl, String naverUrl, LocalDateTime pubDate) {
        this.category = category;
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.naverUrl = naverUrl;
        this.pubDate = pubDate;
    }

    public void linkSummarized(SummarizedNews summarizedNews) {
        this.summarizedNews = summarizedNews;
    }
}
