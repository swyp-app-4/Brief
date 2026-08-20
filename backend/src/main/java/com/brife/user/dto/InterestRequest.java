package com.brife.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterestRequest {

    private List<Long> categoryIds;
    private List<Long> groupIds;
}
