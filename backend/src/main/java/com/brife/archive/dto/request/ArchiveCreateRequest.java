package com.brife.archive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArchiveCreateRequest {

    @NotBlank(message = "폴더 이름을 입력해주세요.")
    @Size(max = 20, message = "폴더 이름은 최대 20자까지 입력할 수 있습니다.")
    private String folderName;
}