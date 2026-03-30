package com.brife.archive.dto.response;

import lombok.Getter;

@Getter
public class ArchiveStatsResponse {

    private final long totalCount;   // 전체 저장 카드 수
    private final long weeklyCount;  // 이번 주 저장 수
    private final long folderCount;  // 폴더 수

    public ArchiveStatsResponse(long totalCount, long weeklyCount, long folderCount) {
        this.totalCount = totalCount;
        this.weeklyCount = weeklyCount;
        this.folderCount = folderCount;
    }
}