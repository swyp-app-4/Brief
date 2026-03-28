// [DTO - 요청] 관심사 저장/재설정 요청 (categoryIds 리스트).
package com.brife.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class InterestRequest {

    private List<Long> categoryIds;
}
