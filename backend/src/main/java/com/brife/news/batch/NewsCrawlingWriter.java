package com.brife.news.batch;

import com.brife.news.domain.RawNews;
import com.brife.news.domain.SummarizedNews;
import com.brife.news.dto.ProcessedNewsDto;
import com.brife.news.repository.RawNewsRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class NewsCrawlingWriter implements ItemWriter<ProcessedNewsDto> {

    private final RawNewsRepository rawNewsRepository;
    private final SummarizedNewsRepository summarizedNewsRepository;

    @Override
    public void write(Chunk<? extends ProcessedNewsDto> chunk) throws Exception {
        for (ProcessedNewsDto data : chunk.getItems()) {
            SummarizedNews saved = summarizedNewsRepository.save(
                    SummarizedNews.builder()
                            .category(data.getCategory())
                            .title(truncate(data.getSynthesisResult().getTitle(), 200))
                            .summary(data.getSynthesisResult().getSummary())
                            .body(data.getSectionsJson())
                            .sourceCount(data.getTotalArticleCount())
                            .publishedDate(data.getPublishedDate())
                            .publishedAt(data.getPublishedAt())
                            .build()
            );

            List<RawNews> rawNewsList = data.getNewArticles().stream()
                    .map(raw -> {
                        RawNews entity = RawNews.builder()
                                .category(data.getCategory())
                                .title(raw.getTitle())
                                .sourceUrl(raw.getSourceUrl())
                                .naverUrl(raw.getNaverUrl())
                                .pubDate(raw.getPubDate())
                                .build();
                        entity.linkSummarized(saved);
                        return entity;
                    })
                    .toList();

            rawNewsRepository.saveAll(rawNewsList);

            log.info("[Writer] 완료 - title={}, 신규기사={}건",
                    data.getSynthesisResult().getTitle(), rawNewsList.size());
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
