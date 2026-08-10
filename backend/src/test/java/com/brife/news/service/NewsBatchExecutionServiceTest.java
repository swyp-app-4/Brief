package com.brife.news.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsBatchExecutionServiceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement lockStatement = mock(PreparedStatement.class);
    private final PreparedStatement unlockStatement = mock(PreparedStatement.class);
    private final ResultSet lockResult = mock(ResultSet.class);
    private final JobOperator jobOperator = mock(JobOperator.class);
    private final Job job = mock(Job.class);
    private NewsBatchExecutionService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new NewsBatchExecutionService(dataSource, jobOperator, job);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT pg_try_advisory_lock(?)"))
                .thenReturn(lockStatement);
        when(lockStatement.executeQuery()).thenReturn(lockResult);
    }

    @Test
    void skipsLaunchWhenAnotherBatchOwnsTheLock() throws Exception {
        when(lockResult.next()).thenReturn(true);
        when(lockResult.getBoolean(1)).thenReturn(false);

        assertThat(service.launch())
                .isEqualTo(NewsBatchExecutionService.LaunchResult.ALREADY_RUNNING);

        verify(jobOperator, never()).start(any(Job.class), any(JobParameters.class));
    }

    @Test
    void launchesAndReleasesLockWhenLockIsAvailable() throws Exception {
        when(lockResult.next()).thenReturn(true);
        when(lockResult.getBoolean(1)).thenReturn(true);
        when(connection.prepareStatement("SELECT pg_advisory_unlock(?)"))
                .thenReturn(unlockStatement);
        JobExecution execution = mock(JobExecution.class);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobOperator.start(any(Job.class), any(JobParameters.class))).thenReturn(execution);

        assertThat(service.launch())
                .isEqualTo(NewsBatchExecutionService.LaunchResult.COMPLETED);

        verify(jobOperator).start(any(Job.class), any(JobParameters.class));
        verify(unlockStatement).executeQuery();
    }
}
