package com.brife.news.service;

import com.brife.news.dto.SearchSuggestionResponse;
import com.brife.news.repository.SearchSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchSuggestionService {

    private final SearchSuggestionRepository searchSuggestionRepository;

    // @Cacheable은 public 메서드에 직접 적용 (self-invocation 회피)
    // key는 trim된 keyword 기준 - null safe navigation + Elvis 연산자 사용
    @Cacheable(value = "searchSuggestions", key = "#keyword?.trim() ?: ''")
    @Transactional(readOnly = true)
    public SearchSuggestionResponse getSuggestions(String keyword) {
        String normalized = (keyword == null) ? "" : keyword.trim();
        if (normalized.length() < 2) {
            return new SearchSuggestionResponse(normalized, List.of());
        }
        List<String> titles = searchSuggestionRepository.findTitleSuggestions(normalized, 5);
        return new SearchSuggestionResponse(normalized, titles);
    }
}
