package com.brife.archive.controller;

import com.brife.archive.dto.request.ArchiveCreateRequest;
import com.brife.archive.dto.request.ArchiveItemCreateRequest;
import com.brife.archive.dto.response.ArchiveItemResponse;
import com.brife.archive.dto.response.ArchiveResponse;
import com.brife.archive.dto.response.ArchiveSearchResponse;
import com.brife.archive.dto.response.ArchiveStatsResponse;
import com.brife.archive.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.brife.archive.dto.response.ArchiveSearchResponse;

import java.util.List;

@RestController
@RequestMapping("/archives")
@RequiredArgsConstructor
@Tag(name = "Archive", description = "아카이브 (폴더 관리 / 뉴스 저장)")
public class ArchiveController {

    private final ArchiveService archiveService;

    @Operation(summary = "폴더 목록 조회")
    @GetMapping
    public ResponseEntity<List<ArchiveResponse>> getFolders(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.getFolders(userId));
    }

    @Operation(summary = "새 폴더 만들기")
    @PostMapping
    public ResponseEntity<ArchiveResponse> createFolder(
            Authentication authentication,
            @Valid @RequestBody ArchiveCreateRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.createFolder(userId, request));
    }

    @Operation(summary = "폴더 이름 수정")
    @PatchMapping("/{archiveId}")
    public ResponseEntity<ArchiveResponse> updateFolderName(
            Authentication authentication,
            @PathVariable Long archiveId,
            @Valid @RequestBody ArchiveCreateRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.updateFolderName(userId, archiveId, request));
    }

    @Operation(summary = "폴더 삭제")
    @DeleteMapping("/{archiveId}")
    public ResponseEntity<Void> deleteFolder(
            Authentication authentication,
            @PathVariable Long archiveId) {
        Long userId = Long.parseLong(authentication.getName());
        archiveService.deleteFolder(userId, archiveId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "폴더 안의 뉴스 목록 조회")
    @GetMapping("/{archiveId}/items")
    public ResponseEntity<List<ArchiveItemResponse>> getItems(
            Authentication authentication,
            @PathVariable Long archiveId,
            @RequestParam(defaultValue = "latest") String sort) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.getItems(userId, archiveId, sort));
    }

    @Operation(summary = "뉴스 저장 (폴더 지정)")
    @PostMapping("/{archiveId}/items")
    public ResponseEntity<ArchiveItemResponse> saveItem(
            Authentication authentication,
            @PathVariable Long archiveId,
            @Valid @RequestBody ArchiveItemCreateRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.saveItem(userId, archiveId, request));
    }

    @Operation(summary = "뉴스 일괄 삭제")
    @DeleteMapping("/{archiveId}/items")
    public ResponseEntity<Void> deleteAllItems(
            Authentication authentication,
            @PathVariable Long archiveId) {
        Long userId = Long.parseLong(authentication.getName());
        archiveService.deleteAllItems(userId, archiveId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "뉴스 개별 삭제")
    @DeleteMapping("/{archiveId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            Authentication authentication,
            @PathVariable Long archiveId,
            @PathVariable Long itemId) {
        Long userId = Long.parseLong(authentication.getName());
        archiveService.deleteItem(userId, archiveId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "즐겨찾기에 뉴스 저장")
    @PostMapping("/favorite/items")
    public ResponseEntity<ArchiveItemResponse> saveToFavorite(
            Authentication authentication,
            @Valid @RequestBody ArchiveItemCreateRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.saveToFavorite(userId, request));
    }

    @Operation(summary = "아카이브 검색 (폴더명 + 뉴스 제목)")
    @GetMapping("/search")
    public ResponseEntity<ArchiveSearchResponse> search(
            Authentication authentication,
            @RequestParam String keyword) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.search(userId, keyword));
    }

    @Operation(summary = "아카이브 통계 조회")
    @GetMapping("/stats")
    public ResponseEntity<ArchiveStatsResponse> getStats(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(archiveService.getStats(userId));
    }
}
