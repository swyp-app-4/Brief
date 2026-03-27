package com.brife.news.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CrawlingScheduler {

    private final JobOperator jobOperator;
    private final Job newsCrawlingJob;

    public CrawlingScheduler(JobOperator jobOperator,
                             @Qualifier("newsCrawlingJob") Job newsCrawlingJob) {
        this.jobOperator = jobOperator;
        this.newsCrawlingJob = newsCrawlingJob;
    }

    // 4시간마다 실행 (0, 4, 8, 12, 16, 20시)
    @Scheduled(cron = "0 0 0/4 * * *", zone = "Asia/Seoul")
    public void runCrawling() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            log.info("[스케줄러] 뉴스 크롤링 Job 시작");
            jobOperator.start(newsCrawlingJob, params);
            log.info("[스케줄러] 뉴스 크롤링 Job 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("[스케줄러] 이전 배치가 아직 실행 중입니다. 이번 스케줄은 건너뜁니다.");
        } catch (Exception e) {
            log.error("[스케줄러] 크롤링 Job 실패", e);
        }
    }
}
