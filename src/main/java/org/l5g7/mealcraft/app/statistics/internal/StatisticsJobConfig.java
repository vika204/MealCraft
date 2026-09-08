package org.l5g7.mealcraft.app.statistics.internal;

import org.l5g7.mealcraft.app.statistics.StatisticsService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Calendar;
import java.util.Date;

@Configuration
public class StatisticsJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StatisticsService statisticsService;

    public StatisticsJobConfig(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               StatisticsService statisticsService) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.statisticsService = statisticsService;
    }

    @Bean
    public Job dailyStatsJob() {
        return new JobBuilder("dailyStatsJob", jobRepository)
                .start(cleanupOldStatsStep())
                .next(calculateDailyStatsStep())
                .build();
    }

    @Bean
    public Step cleanupOldStatsStep() {
        return new StepBuilder("cleanupOldStatsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    statisticsService.cleanupOldStats();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step calculateDailyStatsStep() {
        return new StepBuilder("calculateDailyStatsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters params = contribution.getStepExecution().getJobParameters();
                    Long millis = params.getLong("targetDate");
                    Date targetDay;

                    if (millis != null) {
                        targetDay = new Date(millis);
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, -1);
                        targetDay = cal.getTime();
                    }

                    statisticsService.recalcStatsForDay(targetDay);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
