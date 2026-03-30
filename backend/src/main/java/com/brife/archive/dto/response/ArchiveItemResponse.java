package com.brife.archive.dto.response;

import com.brife.archive.entity.ArchiveItem;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArchiveItemResponse {

    private final Long id;
    private final Long contentId;
    private final LocalDateTime savedAt;

    public ArchiveItemResponse(ArchiveItem archiveItem) {
        this.id = archiveItem.getId();
        this.contentId = archiveItem.getContentId();
        this.savedAt = archiveItem.getSavedAt();
    }
}