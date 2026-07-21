package com.brife.news.controller;

import com.brife.news.domain.SummarizedNews;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import com.brife.news.service.EmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "배치 관리", description = "배치 수동 실행 및 임베딩 생성")
@Slf4j
@RestController
@RequestMapping("/admin/batch")
@RequiredArgsConstructor
public class BatchAdminController {

    private final JobOperator jobOperator;

    @Qualifier("newsCrawlingJob")
    private final Job newsCrawlingJob;

    private final EmbeddingService embeddingService;
    private final NewsEmbeddingRepository newsEmbeddingRepository;
    private final SummarizedNewsRepository summarizedNewsRepository;

    @Operation(summary = "뉴스 크롤링 배치 수동 실행")
    @PostMapping("/crawling")
    public ResponseEntity<String> triggerCrawling() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.start(newsCrawlingJob, params);
            return ResponseEntity.ok("배치 시작");
        } catch (Exception e) {
            log.error("[BatchAdmin] 배치 실행 실패", e);
            return ResponseEntity.internalServerError().body("실패: " + e.getMessage());
        }
    }

    @Operation(summary = "특정 뉴스 ID 목록의 임베딩 생성")
    @PostMapping("/embed")
    public ResponseEntity<String> embedNews(@RequestParam List<Long> ids) {
        List<String> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                SummarizedNews news = summarizedNewsRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("뉴스 없음: " + id));
                float[] embedding = embeddingService.embedForDocument(news.getTitle() + " " + news.getSummary());
                newsEmbeddingRepository.save(id, embedding);
                results.add("OK id=" + id + " (" + embedding.length + "차원)");
            } catch (Exception e) {
                results.add("FAIL id=" + id + " - " + e.getMessage());
            }
        }
        return ResponseEntity.ok(String.join("\n", results));
    }
}
