package com.brife.news.service;

import com.brife.news.domain.Category;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SparseCategoryQueryPolicy {

    private static final Map<String, QueryRule> QUERY_RULES = Map.of(
            "세계일반", new QueryRule(
                    "국제 정상회담|유엔 결의|해외 분쟁 발생|해외 재난 발생",
                    Set.of("국제", "외신|특파원|유엔|UN", "세계일반")),
            "지역", new QueryRule(
                    "지자체 정책 발표|지방의회 조례 의결|지역 축제 개막|지역 재난 사고",
                    Set.of("지자체", "지자체|지방의회", "지역")),
            "청와대", new QueryRule(
                    "대통령실 브리핑|대통령 국무회의|대통령 정상회담|대통령 인사 발표",
                    Set.of("대통령실", "대통령실|대통령|용산", "청와대")),
            "IT일반", new QueryRule(
                    "AI 신제품 출시|AI 서비스 공개|빅테크 기술 발표|과기정통부 AI 정책",
                    Set.of("AI", "인공지능", "인공지능|AI|빅데이터", "IT일반")));

    public String resolve(Category category) {
        if (category == null || category.getName() == null) return "";
        String storedQuery = category.getEffectiveQuery();
        QueryRule rule = QUERY_RULES.get(category.getName());
        if (rule == null || !rule.legacyQueries().contains(storedQuery)) return storedQuery;
        return rule.eventQuery();
    }

    private record QueryRule(String eventQuery, Set<String> legacyQueries) {
    }
}
