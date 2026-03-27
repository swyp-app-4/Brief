package com.brife.news.config;

import com.brife.news.batch.NewsCrawlingProcessor;
import com.brife.news.batch.NewsCrawlingReader;
import com.brife.news.batch.NewsCrawlingWriter;
import com.brife.news.dto.KeywordGroupDto;
import com.brife.news.dto.ProcessedNewsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.dao.DataAccessException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NewsCrawlingJobConfig {

    private static final int MAX_SKIP_COUNT = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job newsCrawlingJob(Step newsCrawlingStep) {
        return new JobBuilder("newsCrawlingJob", jobRepository)
                .start(newsCrawlingStep)
                .build();
    }

    @Bean
    public Step newsCrawlingStep(NewsCrawlingReader reader,
                                  NewsCrawlingProcessor processor,
                                  NewsCrawlingWriter writer) {
        return new StepBuilder("newsCrawlingStep", jobRepository)
                .<KeywordGroupDto, ProcessedNewsDto>chunk(1)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .transactionManager(transactionManager)
                .faultTolerant()
                .skipPolicy((t, skipCount) ->
                        !(t instanceof DataAccessException) && t instanceof RuntimeException && skipCount < MAX_SKIP_COUNT)
                .listener(new SkipListener<KeywordGroupDto, ProcessedNewsDto>() {
                    @Override
                    public void onSkipInProcess(KeywordGroupDto item, Throwable t) {
                        log.warn("[배치] 스킵 - keyword={}, reason={}", item.getKeyword(), t.getMessage());
                    }
                })
                .build();
    }
}
