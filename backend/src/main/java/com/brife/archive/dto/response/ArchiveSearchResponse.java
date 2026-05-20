package com.brife.archive.dto.response;

import lombok.Getter;
import java.util.List;

@Getter
public class ArchiveSearchResponse {
    private final List<ArchiveResponse> folders;
    private final List<ArchiveItemResponse> items;

    public ArchiveSearchResponse(List<ArchiveResponse> folders, List<ArchiveItemResponse> items) {
        this.folders = folders;
        this.items = items;
    }
}