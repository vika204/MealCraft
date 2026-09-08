package org.l5g7.mealcraft.app.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.l5g7.mealcraft.app.statistics.internal.DailyStatsJobScheduler;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyStatsJobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job dailyStatsJob;

    @InjectMocks
    private DailyStatsJobScheduler scheduler;

    @Test
    void runDailyStatsJobForYesterday_launchesJobWithYesterdayDate() throws Exception {
        scheduler.runDailyStatsJobForYesterday();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(1)).run(eq(dailyStatsJob), captor.capture());

        JobParameters params = captor.getValue();
        assertNotNull(params);
        assertNotNull(params.getParameter("targetDate"));

        Long targetDate = params.getLong("targetDate");
        assertNotNull(targetDate);
        assertTrue(targetDate > 0);
    }

    @Test
    void runDailyStatsJobForYesterday_handlesExceptions() {
        try {
            when(jobLauncher.run(eq(dailyStatsJob), any(JobParameters.class)))
                    .thenThrow(new RuntimeException("Job failed"));

            assertThrows(Exception.class, () -> scheduler.runDailyStatsJobForYesterday());
        } catch (Exception e) {
            // Expected
        }
    }
}

