package com.brife.news.service;

import com.brife.news.domain.Category;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SparseCategoryQueryPolicyTest {

    private final SparseCategoryQueryPolicy policy = new SparseCategoryQueryPolicy();

    @Test
    void replacesSparseCategoryQueriesWithEventFocusedQueries() {
        assertThat(policy.resolve(category("세계일반", "국제")))
                .isEqualTo("국제 정상회담|유엔 결의|해외 분쟁 발생|해외 재난 발생");
        assertThat(policy.resolve(category("지역", "지자체")))
                .isEqualTo("지자체 정책 발표|지방의회 조례 의결|지역 축제 개막|지역 재난 사고");
        assertThat(policy.resolve(category("청와대", "대통령실")))
                .isEqualTo("대통령실 브리핑|대통령 국무회의|대통령 정상회담|대통령 인사 발표");
        assertThat(policy.resolve(category("IT일반", "AI")))
                .isEqualTo("AI 신제품 출시|AI 서비스 공개|빅테크 기술 발표|과기정통부 AI 정책");
    }

    @Test
    void keepsStoredQueryForOtherCategories() {
        Category category = category("금융", "금리|은행");

        assertThat(policy.resolve(category)).isEqualTo("금리|은행");
    }

    @Test
    void keepsOperatorCustomizedQueryForSparseCategory() {
        Category category = category("IT일반", "AI 로봇 출시|퀘텀컴퓨팅 투자");

        assertThat(policy.resolve(category)).isEqualTo("AI 로봇 출시|퀘텀컴퓨팅 투자");
    }

    @Test
    void eachSparseCategoryUsesFourDistinctQueries() {
        for (String categoryName : List.of("세계일반", "지역", "청와대", "IT일반")) {
            assertThat(policy.resolve(category(categoryName, categoryName)).split("\\|"))
                    .hasSize(4)
                    .doesNotHaveDuplicates()
                    .allMatch(query -> query.contains(" "));
        }
    }

    private Category category(String name, String query) {
        return Category.builder().name(name).query(query).build();
    }
}
