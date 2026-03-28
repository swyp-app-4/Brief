// [DTO - 요청] 관심사 저장/재설정 요청. categoryIds(소분류), groupIds(대분류만 선택 시). 혼합 가능.
package com.brife.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class InterestRequest {

    private List<Long> categoryIds;
    private List<Long> groupIds;
}
