package com.brife.archive.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "archive")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Archive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "folder_name", nullable = false, length = 20)
    private String folderName;

    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "archive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArchiveItem> items = new ArrayList<>();

    @Builder
    public Archive(Long userId, String folderName, boolean isFavorite) {
        this.userId = userId;
        this.folderName = folderName;
        this.isFavorite = isFavorite;
    }

    // 폴더 이름 수정 (즐겨찾기 폴더는 수정 불가)
    public void updateFolderName(String folderName) {
        if (this.isFavorite) {
            throw new IllegalStateException("즐겨찾기 폴더의 이름은 수정할 수 없습니다.");
        }
        this.folderName = folderName;
    }
}