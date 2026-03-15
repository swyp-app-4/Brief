package com.brife.archive.controller;

import com.brife.archive.dto.request.ArchiveCreateRequest;
import com.brife.archive.dto.request.ArchiveItemCreateRequest;
import com.brife.archive.dto.response.ArchiveItemResponse;
import com.brife.archive.dto.response.ArchiveResponse;
import com.brife.archive.dto.response.ArchiveStatsResponse;
import com.brife.archive.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/archives")
@RequiredArgsConstructor
@Tag(name = "Archive", description = "아카이브 (폴더 관리 / 뉴스 저장)")
public class ArchiveController {

    private final ArchiveService archiveService;

    // 임시 userId (나중에 Security 연동 후 교체)
    private static final Long TEMP_USER_ID = 1L;

    @Operation(summary = "폴더 목록 조회")
    @GetMapping
    public ResponseEntity<List<ArchiveResponse>> getFolders() {
        return ResponseEntity.ok(archiveService.getFolders(TEMP_USER_ID));
    }

    @Operation(summary = "새 폴더 만들기")
    @PostMapping
    public ResponseEntity<ArchiveResponse> createFolder(
            @Valid @RequestBody ArchiveCreateRequest request) {
        return ResponseEntity.ok(archiveService.createFolder(TEMP_USER_ID, request));
    }

    @Operation(summary = "폴더 이름 수정")
    @PatchMapping("/{archiveId}")
    public ResponseEntity<ArchiveResponse> updateFolderName(
            @PathVariable Long archiveId,
            @Valid @RequestBody ArchiveCreateRequest request) {
        return ResponseEntity.ok(archiveService.updateFolderName(TEMP_USER_ID, archiveId, request));
    }

    @Operation(summary = "폴더 삭제")
    @DeleteMapping("/{archiveId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long archiveId) {
        archiveService.deleteFolder(TEMP_USER_ID, archiveId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "폴더 안의 뉴스 목록 조회")
    @GetMapping("/{archiveId}/items")
    public ResponseEntity<List<ArchiveItemResponse>> getItems(@PathVariable Long archiveId) {
        return ResponseEntity.ok(archiveService.getItems(TEMP_USER_ID, archiveId));
    }

    @Operation(summary = "뉴스 저장 (폴더 지정)")
    @PostMapping("/{archiveId}/items")
    public ResponseEntity<ArchiveItemResponse> saveItem(
            @PathVariable Long archiveId,
            @Valid @RequestBody ArchiveItemCreateRequest request) {
        return ResponseEntity.ok(archiveService.saveItem(TEMP_USER_ID, archiveId, request));
    }

    @Operation(summary = "뉴스 일괄 삭제")
    @DeleteMapping("/{archiveId}/items")
    public ResponseEntity<Void> deleteAllItems(@PathVariable Long archiveId) {
        archiveService.deleteAllItems(TEMP_USER_ID, archiveId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "뉴스 개별 삭제")
    @DeleteMapping("/{archiveId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long archiveId,
            @PathVariable Long itemId) {
        archiveService.deleteItem(TEMP_USER_ID, archiveId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "즐겨찾기에 뉴스 저장")
    @PostMapping("/favorite/items")
    public ResponseEntity<ArchiveItemResponse> saveToFavorite(
            @Valid @RequestBody ArchiveItemCreateRequest request) {
        return ResponseEntity.ok(archiveService.saveToFavorite(TEMP_USER_ID, request));
    }

    @Operation(summary = "아카이브 통계 조회")
    @GetMapping("/stats")
    public ResponseEntity<ArchiveStatsResponse> getStats() {
        return ResponseEntity.ok(archiveService.getStats(TEMP_USER_ID));
    }
}