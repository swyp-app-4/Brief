package com.brife.news.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import static org.assertj.core.api.Assertions.assertThat;

class CrawlingSchedulerTest {

    @Test
    void runsBeforeEachDailyNotificationWindow() throws Exception {
        Scheduled scheduled = CrawlingScheduler.class
                .getDeclaredMethod("runCrawling")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled.cron()).isEqualTo("0 30 5,9,15,19 * * *");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
    }
}
