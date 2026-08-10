package com.brife.news.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Service
public class NewsBatchExecutionService {

    private static final long NEWS_BATCH_LOCK_KEY = 724234596031L;

    private final DataSource dataSource;
    private final JobOperator jobOperator;
    private final Job newsCrawlingJob;

    public NewsBatchExecutionService(DataSource dataSource,
                                     JobOperator jobOperator,
                                     @Qualifier("newsCrawlingJob") Job newsCrawlingJob) {
        this.dataSource = dataSource;
        this.jobOperator = jobOperator;
        this.newsCrawlingJob = newsCrawlingJob;
    }

    public LaunchResult launch() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!tryLock(connection)) {
                return LaunchResult.ALREADY_RUNNING;
            }
            try {
                JobParameters parameters = new JobParametersBuilder()
                        .addLong("run.id", System.currentTimeMillis())
                        .toJobParameters();
                JobExecution execution = jobOperator.start(newsCrawlingJob, parameters);
                return execution.getStatus() == BatchStatus.COMPLETED
                        ? LaunchResult.COMPLETED
                        : LaunchResult.FAILED;
            } finally {
                unlock(connection);
            }
        }
    }

    private boolean tryLock(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pg_try_advisory_lock(?)")) {
            statement.setLong(1, NEWS_BATCH_LOCK_KEY);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        }
    }

    private void unlock(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pg_advisory_unlock(?)")) {
            statement.setLong(1, NEWS_BATCH_LOCK_KEY);
            statement.executeQuery();
        } catch (SQLException e) {
            log.error("[Batch] Failed to release advisory lock", e);
        }
    }

    public enum LaunchResult {
        COMPLETED,
        FAILED,
        ALREADY_RUNNING
    }
}
