package org.l5g7.mealcraft.app.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.l5g7.mealcraft.app.statistics.internal.DailyStats;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job dailyStatsJob;

    @InjectMocks
    private StatisticsController controller;

    @Test
    void getStats_withoutDay_returnsAllStats() {
        List<DailyStats> allStats = List.of(
                DailyStats.builder().id(1L).newUsersCount(5L).build(),
                DailyStats.builder().id(2L).newUsersCount(3L).build()
        );
        when(statisticsService.getAllStats()).thenReturn(allStats);

        ResponseEntity<List<DailyStats>> response = controller.getStats(null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(statisticsService, times(1)).getAllStats();
    }

    @Test
    void getStats_withBlankDay_returnsAllStats() {
        List<DailyStats> allStats = List.of(
                DailyStats.builder().id(1L).newUsersCount(5L).build()
        );
        when(statisticsService.getAllStats()).thenReturn(allStats);

        ResponseEntity<List<DailyStats>> response = controller.getStats("");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(statisticsService, times(1)).getAllStats();
    }

    @Test
    void getStats_withValidDay_returnsStatsForDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JANUARY, 15, 0, 0, 0);
        Date testDate = cal.getTime();

        DailyStats stats = DailyStats.builder()
                .id(1L)
                .day(testDate)
                .newUsersCount(10L)
                .build();

        when(statisticsService.getStatsForDay(any(Date.class))).thenReturn(stats);

        ResponseEntity<List<DailyStats>> response = controller.getStats("2025-01-15");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).getNewUsersCount());
    }

    @Test
    void getStats_withValidDay_whenNoStats_returnsEmptyList() {
        when(statisticsService.getStatsForDay(any(Date.class))).thenReturn(null);

        ResponseEntity<List<DailyStats>> response = controller.getStats("2025-01-15");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getStats_withInvalidDay_returnsEmptyList() {
        ResponseEntity<List<DailyStats>> response = controller.getStats("invalid-date");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
        verify(statisticsService, never()).getStatsForDay(any());
    }

    @Test
    void recalculateStats_withoutDay_launchesJobForYesterday() throws Exception {
        when(jobLauncher.run(eq(dailyStatsJob), any(JobParameters.class))).thenReturn(null);

        ResponseEntity<Void> response = controller.recalculateStats(null);

        assertEquals(202, response.getStatusCode().value());
        verify(jobLauncher, times(1)).run(eq(dailyStatsJob), any(JobParameters.class));
    }

    @Test
    void recalculateStats_withDay_launchesJobForSpecificDay() throws Exception {
        when(jobLauncher.run(eq(dailyStatsJob), any(JobParameters.class))).thenReturn(null);

        ResponseEntity<Void> response = controller.recalculateStats("2025-01-15");

        assertEquals(202, response.getStatusCode().value());
        verify(jobLauncher, times(1)).run(eq(dailyStatsJob), any(JobParameters.class));
    }

    @Test
    void recalculateStats_withInvalidDay_returns400() throws Exception {
        ResponseEntity<Void> response = controller.recalculateStats("invalid-date");

        assertEquals(400, response.getStatusCode().value());
        verify(jobLauncher, never()).run(any(), any());
    }
}

