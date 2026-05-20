package com.brife.archive.repository;

import com.brife.archive.entity.ArchiveItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArchiveItemRepository extends JpaRepository<ArchiveItem, Long> {

    // 폴더 안의 아이템 목록 조회
    List<ArchiveItem> findByArchiveId(Long archiveId);

    // 유저의 전체 저장 카드 수 (통계용)
    @Query("SELECT COUNT(ai) FROM ArchiveItem ai WHERE ai.archive.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // 유저의 이번 주 저장 카드 수 (통계용)
    @Query("SELECT COUNT(ai) FROM ArchiveItem ai WHERE ai.archive.userId = :userId AND ai.savedAt >= :startOfWeek")
    long countByUserIdAndSavedAtAfter(@Param("userId") Long userId, @Param("startOfWeek") LocalDateTime startOfWeek);

    // 특정 폴더에 특정 뉴스 이미 저장됐는지 확인 (중복 저장 방지)
    boolean existsByArchiveIdAndContentId(Long archiveId, Long contentId);

    // 최신순
    List<ArchiveItem> findByArchiveIdOrderBySavedAtDesc(Long archiveId);

    // 오래된순
    List<ArchiveItem> findByArchiveIdOrderBySavedAtAsc(Long archiveId);

    // 유저의 보관함에서 뉴스 제목으로 검색
    @Query("SELECT ai FROM ArchiveItem ai JOIN SummarizedNews sn ON ai.contentId = sn.id WHERE ai.archive.userId = :userId AND sn.title LIKE %:keyword%")
    List<ArchiveItem> searchByNewsTitleAndUserId(@Param("userId") Long userId, @Param("keyword") String keyword);

// 이름순 (나중에 summarized_news 조인 필요 - 일단 보류)
}