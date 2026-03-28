package com.brife.archive.dto.response;

import com.brife.archive.entity.Archive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArchiveResponse {

    private final Long id;
    private final String folderName;
    private final boolean isFavorite;
    private final int itemCount;
    private final LocalDateTime createdAt;

    public ArchiveResponse(Archive archive) {
        this.id = archive.getId();
        this.folderName = archive.getFolderName();
        this.isFavorite = archive.isFavorite();
        this.itemCount = archive.getItems().size();
        this.createdAt = archive.getCreatedAt();
    }
}