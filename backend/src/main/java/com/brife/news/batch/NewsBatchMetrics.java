package com.brife.news.batch;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 배치 실행 계측 지표 (singleton).
 * 주의: Job이 동시 실행되면 여러 Job의 지표가 섞일 수 있습니다.
 * 현재 구조에서는 동일 Job 중복 실행을 막고 있으므로 singleton으로 충분합니다.
 */
@Component
public class NewsBatchMetrics {

    private final AtomicInteger naverApiCallCount          = new AtomicInteger();
    private final AtomicInteger fetchedArticleCount        = new AtomicInteger();
    private final AtomicInteger originalExtractSuccessCount = new AtomicInteger();
    private final AtomicInteger originalExtractFallbackCount = new AtomicInteger();
    private final AtomicLong    totalOriginalTextLength    = new AtomicLong();
    private final AtomicInteger clusterCount               = new AtomicInteger();
    private final AtomicInteger skippedClusterCount        = new AtomicInteger();
    private final AtomicInteger vertexCallCount            = new AtomicInteger();
    private final AtomicInteger vertexRetryCount           = new AtomicInteger();
    private final AtomicInteger generatedNewsCount         = new AtomicInteger();
    private final AtomicInteger embeddingSuccessCount      = new AtomicInteger();
    private final AtomicInteger embeddingFailCount         = new AtomicInteger();

    // ── increment ─────────────────────────────────────────────────────────
    public void incrementNaverApiCallCount()           { naverApiCallCount.incrementAndGet(); }
    public void addFetchedArticleCount(int n)          { fetchedArticleCount.addAndGet(n); }
    public void incrementOriginalExtractSuccessCount()  { originalExtractSuccessCount.incrementAndGet(); }
    public void incrementOriginalExtractFallbackCount() { originalExtractFallbackCount.incrementAndGet(); }
    public void addTotalOriginalTextLength(int n)      { totalOriginalTextLength.addAndGet(n); }
    public void addClusterCount(int n)                 { clusterCount.addAndGet(n); }
    public void incrementSkippedClusterCount()         { skippedClusterCount.incrementAndGet(); }
    public void incrementVertexCallCount()             { vertexCallCount.incrementAndGet(); }
    public void incrementVertexRetryCount()            { vertexRetryCount.incrementAndGet(); }
    public void incrementGeneratedNewsCount()          { generatedNewsCount.incrementAndGet(); }
    public void incrementEmbeddingSuccessCount()       { embeddingSuccessCount.incrementAndGet(); }
    public void incrementEmbeddingFailCount()          { embeddingFailCount.incrementAndGet(); }

    // ── get ───────────────────────────────────────────────────────────────
    public int  getNaverApiCallCount()           { return naverApiCallCount.get(); }
    public int  getFetchedArticleCount()         { return fetchedArticleCount.get(); }
    public int  getOriginalExtractSuccessCount() { return originalExtractSuccessCount.get(); }
    public int  getOriginalExtractFallbackCount(){ return originalExtractFallbackCount.get(); }
    public long getTotalOriginalTextLength()     { return totalOriginalTextLength.get(); }
    public int  getClusterCount()                { return clusterCount.get(); }
    public int  getSkippedClusterCount()         { return skippedClusterCount.get(); }
    public int  getVertexCallCount()             { return vertexCallCount.get(); }
    public int  getVertexRetryCount()            { return vertexRetryCount.get(); }
    public int  getGeneratedNewsCount()          { return generatedNewsCount.get(); }
    public int  getEmbeddingSuccessCount()       { return embeddingSuccessCount.get(); }
    public int  getEmbeddingFailCount()          { return embeddingFailCount.get(); }

    // ── summary helpers ───────────────────────────────────────────────────
    /** 원문 추출 성공률 (0 ~ 1.0). 분모가 0이면 0.0 반환. */
    public double getExtractSuccessRate() {
        int total = originalExtractSuccessCount.get() + originalExtractFallbackCount.get();
        return total == 0 ? 0.0 : (double) originalExtractSuccessCount.get() / total;
    }

    /** 평균 원문 텍스트 길이(char). 분모가 0이면 0 반환. */
    public long getAvgOriginalTextLength() {
        int total = originalExtractSuccessCount.get() + originalExtractFallbackCount.get();
        return total == 0 ? 0L : totalOriginalTextLength.get() / total;
    }

    // ── reset ─────────────────────────────────────────────────────────────
    public void reset() {
        naverApiCallCount.set(0);
        fetchedArticleCount.set(0);
        originalExtractSuccessCount.set(0);
        originalExtractFallbackCount.set(0);
        totalOriginalTextLength.set(0);
        clusterCount.set(0);
        skippedClusterCount.set(0);
        vertexCallCount.set(0);
        vertexRetryCount.set(0);
        generatedNewsCount.set(0);
        embeddingSuccessCount.set(0);
        embeddingFailCount.set(0);
    }
}
