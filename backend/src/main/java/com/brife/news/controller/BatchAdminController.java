package com.brife.news.controller;

import com.brife.news.domain.SummarizedNews;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import com.brife.news.service.EmbeddingService;
import com.brife.news.service.NewsBatchExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Batch management", description = "Manual news batch and embedding operations")
@Slf4j
@RestController
@RequestMapping("/admin/batch")
@RequiredArgsConstructor
public class BatchAdminController {

    private final NewsBatchExecutionService newsBatchExecutionService;
    private final EmbeddingService embeddingService;
    private final NewsEmbeddingRepository newsEmbeddingRepository;
    private final SummarizedNewsRepository summarizedNewsRepository;

    @Operation(summary = "Run the news crawling batch manually")
    @PostMapping("/crawling")
    public ResponseEntity<String> triggerCrawling() {
        try {
            NewsBatchExecutionService.LaunchResult result = newsBatchExecutionService.launch();
            if (result == NewsBatchExecutionService.LaunchResult.ALREADY_RUNNING) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("News batch is already running");
            }
            if (result == NewsBatchExecutionService.LaunchResult.FAILED) {
                return ResponseEntity.internalServerError().body("News batch failed");
            }
            return ResponseEntity.ok("News batch completed");
        } catch (Exception e) {
            log.error("[BatchAdmin] News batch failed", e);
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Create embeddings for specified news IDs")
    @PostMapping("/embed")
    public ResponseEntity<String> embedNews(@RequestParam List<Long> ids) {
        List<String> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                SummarizedNews news = summarizedNewsRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("News not found: " + id));
                float[] embedding = embeddingService.embedForDocument(news.getTitle() + " " + news.getSummary());
                newsEmbeddingRepository.save(id, embedding);
                results.add("OK id=" + id + " (dimensions=" + embedding.length + ")");
            } catch (Exception e) {
                results.add("FAIL id=" + id + " - " + e.getMessage());
            }
        }
        return ResponseEntity.ok(String.join("\n", results));
    }
}
