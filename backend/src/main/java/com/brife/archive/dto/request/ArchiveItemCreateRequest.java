package com.brife.archive.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArchiveItemCreateRequest {

    @NotNull(message = "저장할 뉴스 ID를 입력해주세요.")
    private Long contentId;
}