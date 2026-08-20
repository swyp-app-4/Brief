package com.brife.news.batch;

import com.brife.news.service.DuplicateNewsDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchCompletionListener implements JobExecutionListener {

    private final CacheManager cacheManager;
    private final BatchMetadataHolder batchMetadataHolder;
    private final NewsBatchMetrics batchMetrics;
    private final DuplicateNewsDetectionService duplicateNewsDetectionService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        duplicateNewsDetectionService.resetBatchCandidates();
        LocalDateTime startedAt = LocalDateTime.now();
        batchMetadataHolder.updateStartedAt(startedAt);
        log.info("[배치] 시작 → 시작 시각 기록. startedAt={}", startedAt);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        batchMetadataHolder.markFinished();

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            LocalDateTime completedAt = LocalDateTime.now();
            batchMetadataHolder.updateCompletedAt(completedAt);
            Cache top5NewsCache = cacheManager.getCache("top5News");
            if (top5NewsCache != null) top5NewsCache.clear();
            Cache searchSuggestionsCache = cacheManager.getCache("searchSuggestions");
            if (searchSuggestionsCache != null) searchSuggestionsCache.clear();
            log.info("[배치] 완료 → Top5 캐시, 검색어 자동완성 캐시 초기화. completedAt={}", completedAt);
        } else {
            log.warn("[배치] 비정상 종료 (status={}) → 캐시 유지", jobExecution.getStatus());
        }

        log.info("[BatchMetrics] status={}, naverCalls={}, fetchedArticles={}, clusters={}, skippedClusters={}, " +
                 "vertexCalls={}, vertexRetries={}, generatedNews={}, extractSuccessRate={}, " +
                 "avgOriginalTextLength={}, embeddingSuccess={}, embeddingFail={}",
                jobExecution.getStatus(),
                batchMetrics.getNaverApiCallCount(),
                batchMetrics.getFetchedArticleCount(),
                batchMetrics.getClusterCount(),
                batchMetrics.getSkippedClusterCount(),
                batchMetrics.getVertexCallCount(),
                batchMetrics.getVertexRetryCount(),
                batchMetrics.getGeneratedNewsCount(),
                String.format("%.1f%%", batchMetrics.getExtractSuccessRate() * 100),
                batchMetrics.getAvgOriginalTextLength(),
                batchMetrics.getEmbeddingSuccessCount(),
                batchMetrics.getEmbeddingFailCount());

        batchMetrics.reset();
    }
}
