package com.brife.archive.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "archive_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"archive_id", "content_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    private Archive archive;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @CreationTimestamp
    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

    @Builder
    public ArchiveItem(Archive archive, Long contentId) {
        this.archive = archive;
        this.contentId = contentId;
    }
}