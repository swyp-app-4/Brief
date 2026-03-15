package com.brife.archive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArchiveCreateRequest {

    @NotBlank(message = "폴더명을 입력해주세요.")
    @Size(max = 20, message = "폴더명은 최대 20자까지 입력 가능합니다.")
    private String folderName;
}