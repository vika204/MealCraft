package org.l5g7.mealcraft.app.statistics;

import org.l5g7.mealcraft.app.statistics.internal.DailyStats;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final JobLauncher jobLauncher;
    private final Job dailyStatsJob;

    public StatisticsController(StatisticsService statisticsService,
                                JobLauncher jobLauncher,
                                @Qualifier("dailyStatsJob") Job dailyStatsJob) {
        this.statisticsService = statisticsService;
        this.jobLauncher = jobLauncher;
        this.dailyStatsJob = dailyStatsJob;
    }

    @GetMapping
    public ResponseEntity<List<DailyStats>> getStats(@RequestParam(required = false) String day) {
        if (day == null || day.isBlank()) {
            List<DailyStats> all = statisticsService.getAllStats();
            return ResponseEntity.ok(all);
        }

        Date targetDay = parseDay(day);
        if (targetDay == null) {
            return ResponseEntity.ok(List.of());
        }

        DailyStats stats = statisticsService.getStatsForDay(targetDay);
        if (stats == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(List.of(stats));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculateStats(@RequestParam(required = false) String day) throws JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        Date targetDay;

        if (day == null || day.isBlank()) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            targetDay = cal.getTime();
        } else {
            Date parsed = parseDay(day);
            if (parsed == null) {
                return ResponseEntity.badRequest().build();
            }
            targetDay = parsed;
        }

        JobParameters params = new JobParametersBuilder()
                .addLong("targetDate", targetDay.getTime())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(dailyStatsJob, params);

        return ResponseEntity.accepted().build();
    }

    private Date parseDay(String day) {
        try {
            String[] parts = day.split("-");
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2])
            );
            return cal.getTime();
        } catch (Exception ex) {
            return null;
        }
    }
}
