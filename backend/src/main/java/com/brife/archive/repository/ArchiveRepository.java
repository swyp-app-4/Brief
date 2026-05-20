package com.brife.archive.repository;

import com.brife.archive.entity.Archive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    // 유저의 폴더 목록 전체 조회
    List<Archive> findByUserId(Long userId);

    // 회원 탈퇴 시 유저의 모든 폴더 삭제
    @Modifying
    @Query("DELETE FROM Archive a WHERE a.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // 유저의 즐겨찾기 폴더 조회
    Optional<Archive> findByUserIdAndIsFavoriteTrue(Long userId);

    // 즐겨찾기 폴더 존재 여부 확인
    boolean existsByUserIdAndIsFavoriteTrue(Long userId);

    // 폴더명 중복 확인 (즐겨찾기 이름으로 생성 못하게)
    boolean existsByUserIdAndFolderName(Long userId, String folderName);

    // 폴더명 검색
    List<Archive> findByUserIdAndFolderNameContainingIgnoreCase(Long userId, String keyword);
}
