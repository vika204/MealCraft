package org.l5g7.mealcraft.app.statistics.internal;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class DailyStatsJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailyStatsJob;

    public DailyStatsJobScheduler(JobLauncher jobLauncher, Job dailyStatsJob) {
        this.jobLauncher = jobLauncher;
        this.dailyStatsJob = dailyStatsJob;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyStatsJobForYesterday() throws JobExecutionException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        JobParameters params = new JobParametersBuilder()
                .addLong("targetDate", yesterday.getTime())
                .toJobParameters();

        jobLauncher.run(dailyStatsJob, params);
    }
}
