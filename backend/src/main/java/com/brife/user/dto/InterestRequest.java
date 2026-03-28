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
