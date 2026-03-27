package com.brife.archive.service;

import com.brife.archive.dto.request.ArchiveCreateRequest;
import com.brife.archive.dto.request.ArchiveItemCreateRequest;
import com.brife.archive.dto.response.ArchiveItemResponse;
import com.brife.archive.dto.response.ArchiveResponse;
import com.brife.archive.dto.response.ArchiveStatsResponse;
import com.brife.archive.entity.Archive;
import com.brife.archive.entity.ArchiveItem;
import com.brife.archive.repository.ArchiveItemRepository;
import com.brife.archive.repository.ArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final ArchiveItemRepository archiveItemRepository;

    // 폴더 목록 조회
    public List<ArchiveResponse> getFolders(Long userId) {
        return archiveRepository.findByUserId(userId)
                .stream()
                .map(ArchiveResponse::new)
                .collect(Collectors.toList());
    }

    // 새 폴더 만들기
    @Transactional
    public ArchiveResponse createFolder(Long userId, ArchiveCreateRequest request) {
        String folderName = request.getFolderName();

        // 즐겨찾기 이름으로 폴더 생성 방지
        if (folderName.equals("즐겨찾기")) {
            throw new IllegalArgumentException("즐겨찾기는 사용할 수 없는 폴더명입니다.");
        }

        // 특수문자 방지
        if (!folderName.matches("^[a-zA-Z0-9가-힣\\s]+$")) {
            throw new IllegalArgumentException("특수문자(/, ,)는 폴더 이름에 쓸 수 없어요.");
        }

        // 20자 초과 방지
        if (folderName.length() > 20) {
            throw new IllegalArgumentException("폴더 이름은 최대 20자까지 입력할 수 있습니다.");
        }

        // 같은 이름 폴더 중복 방지
        if (archiveRepository.existsByUserIdAndFolderName(userId, folderName)) {
            throw new IllegalArgumentException("이미 사용중인 폴더 이름이에요.");
        }

        Archive archive = Archive.builder()
                .userId(userId)
                .folderName(folderName)
                .isFavorite(false)
                .build();

        return new ArchiveResponse(archiveRepository.save(archive));
    }

    // 폴더 이름 수정
    @Transactional
    public ArchiveResponse updateFolderName(Long userId, Long archiveId, ArchiveCreateRequest request) {
        String folderName = request.getFolderName();

        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        // 본인 폴더인지 확인
        if (!archive.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 폴더만 수정할 수 있습니다.");
        }

        // 특수문자 방지
        if (!folderName.matches("^[a-zA-Z0-9가-힣\\s]+$")) {
            throw new IllegalArgumentException("특수문자는 폴더 이름에 사용할 수 없습니다.");
        }

        // 20자 초과 방지
        if (folderName.length() > 20) {
            throw new IllegalArgumentException("폴더 이름은 최대 20자까지 입력할 수 있습니다.");
        }

        // 같은 이름 폴더 중복 방지
        if (archiveRepository.existsByUserIdAndFolderName(userId, folderName)) {
            throw new IllegalArgumentException("이미 사용 중인 폴더 이름입니다.");
        }

        archive.updateFolderName(folderName);
        return new ArchiveResponse(archive);
    }

    // 폴더 삭제
    @Transactional
    public void deleteFolder(Long userId, Long archiveId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        // 본인 폴더인지 확인
        if (!archive.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 폴더만 삭제할 수 있습니다.");
        }
        // 즐겨찾기 폴더 삭제 방지
        if (archive.isFavorite()) {
            throw new IllegalArgumentException("즐겨찾기 폴더는 삭제할 수 없습니다.");
        }

        archiveRepository.delete(archive);
    }

    // 폴더 안의 뉴스 목록 조회
    public List<ArchiveItemResponse> getItems(Long userId, Long archiveId, String sort) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if (!archive.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 폴더만 조회할 수 있습니다.");
        }

        List<ArchiveItem> items;
        if (sort.equals("oldest")) {
            items = archiveItemRepository.findByArchiveIdOrderBySavedAtAsc(archiveId);
        } else if (sort.equals("name")) {
            items = archiveItemRepository.findByArchiveIdOrderBySavedAtAsc(archiveId); // 나중에 이름순 추가
        } else {
            items = archiveItemRepository.findByArchiveIdOrderBySavedAtDesc(archiveId);
        }

        return items.stream()
                .map(ArchiveItemResponse::new)
                .collect(Collectors.toList());
    }

    // 뉴스 저장 (폴더 지정)
    @Transactional
    public ArchiveItemResponse saveItem(Long userId, Long archiveId, ArchiveItemCreateRequest request) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if (!archive.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 폴더에만 저장할 수 있습니다.");
        }
        // 중복 저장 방지
        if (archiveItemRepository.existsByArchiveIdAndContentId(archiveId, request.getContentId())) {
            throw new IllegalArgumentException("이미 저장된 뉴스입니다.");
        }

        ArchiveItem item = ArchiveItem.builder()
                .archive(archive)
                .contentId(request.getContentId())
                .build();

        return new ArchiveItemResponse(archiveItemRepository.save(item));
    }

    // 즐겨찾기에 뉴스 저장
    @Transactional
    public ArchiveItemResponse saveToFavorite(Long userId, ArchiveItemCreateRequest request) {
        // 즐겨찾기 폴더 없으면 자동 생성
        Archive favoriteFolder = archiveRepository.findByUserIdAndIsFavoriteTrue(userId)
                .orElseGet(() -> archiveRepository.save(
                        Archive.builder()
                                .userId(userId)
                                .folderName("즐겨찾기")
                                .isFavorite(true)
                                .build()
                ));

        // 중복 저장 방지
        if (archiveItemRepository.existsByArchiveIdAndContentId(favoriteFolder.getId(), request.getContentId())) {
            throw new IllegalArgumentException("이미 즐겨찾기에 저장된 뉴스입니다.");
        }

        ArchiveItem item = ArchiveItem.builder()
                .archive(favoriteFolder)
                .contentId(request.getContentId())
                .build();

        return new ArchiveItemResponse(archiveItemRepository.save(item));
    }

    // 뉴스 개별 삭제
    @Transactional
    public void deleteItem(Long userId, Long archiveId, Long itemId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if (!archive.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 폴더만 수정할 수 있습니다.");
        }

        // 즐겨찾기 폴더는 카드 삭제 불가
        if (archive.isFavorite()) {
            throw new IllegalArgumentException("즐겨찾기 폴더에서는 카드를 삭제할 수 없습니다.");
        }

        ArchiveItem item = archiveItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 뉴스를 찾을 수 없습니다."));

        archiveItemRepository.delete(item);
    }

    // 뉴스 일괄 삭제
    @Transactional
    public void deleteAllItems(Long userId, Long archiveId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if (!archive.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 폴더만 수정할 수 있습니다.");
        }

        // 즐겨찾기 폴더는 카드 삭제 불가
        if (archive.isFavorite()) {
            throw new IllegalArgumentException("즐겨찾기 폴더에서는 카드를 삭제할 수 없습니다.");
        }

        archiveItemRepository.deleteAll(archive.getItems());
    }

    // 아카이브 통계 조회
    public ArchiveStatsResponse getStats(Long userId) {
        long totalCount = archiveItemRepository.countByUserId(userId);
        long folderCount = archiveRepository.findByUserId(userId).size();
        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay();
        long weeklyCount = archiveItemRepository.countByUserIdAndSavedAtAfter(userId, startOfWeek);

        return new ArchiveStatsResponse(totalCount, weeklyCount, folderCount);
    }
}