package com.brife.news.scheduler;

import com.brife.news.service.NewsBatchExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final NewsBatchExecutionService newsBatchExecutionService;

    @Scheduled(cron = "0 30 5,9,15,19 * * *", zone = "Asia/Seoul")
    public void runCrawling() {
        try {
            log.info("[Scheduler] Starting news crawling job");
            NewsBatchExecutionService.LaunchResult result = newsBatchExecutionService.launch();
            if (result == NewsBatchExecutionService.LaunchResult.ALREADY_RUNNING) {
                log.warn("[Scheduler] News batch is already running; scheduled launch skipped");
                return;
            }
            if (result == NewsBatchExecutionService.LaunchResult.FAILED) {
                log.error("[Scheduler] News crawling job finished with a failure status");
                return;
            }
            log.info("[Scheduler] News crawling job completed");
        } catch (Exception e) {
            log.error("[Scheduler] News crawling job failed", e);
        }
    }
}
