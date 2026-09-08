package org.l5g7.mealcraft.app.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.recipes.RecipeRepository;
import org.l5g7.mealcraft.app.statistics.internal.DailyStats;
import org.l5g7.mealcraft.app.statistics.internal.DailyStatsRepository;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private DailyStatsRepository dailyStatsRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private Date testDate;

    @BeforeEach
    void setUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        testDate = cal.getTime();
    }

    @Test
    void recalcStatsForDay_createsNewStats_whenNotExists() {
        when(dailyStatsRepository.findByDay(any(Date.class))).thenReturn(Optional.empty());
        when(userRepository.countByCreatedAtBetween(any(Date.class), any(Date.class))).thenReturn(5L);
        when(productRepository.countByCreatedAtBetween(any(Date.class), any(Date.class))).thenReturn(3L);
        when(recipeRepository.countByCreatedAtBetween(any(Date.class), any(Date.class))).thenReturn(2L);

        statisticsService.recalcStatsForDay(testDate);

        ArgumentCaptor<DailyStats> captor = ArgumentCaptor.forClass(DailyStats.class);
        verify(dailyStatsRepository, times(1)).save(captor.capture());

        DailyStats saved = captor.getValue();
        assertEquals(5L, saved.getNewUsersCount());
        assertEquals(3L, saved.getNewProductsCount());
        assertEquals(2L, saved.getNewRecipesCount());
        assertNotNull(saved.getDay());
    }

    @Test
    void recalcStatsForDay_updatesExistingStats() {
        DailyStats existing = DailyStats.builder()
                .id(1L)
                .day(testDate)
                .newUsersCount(0L)
                .newProductsCount(0L)
                .newRecipesCount(0L)
                .build();

        when(dailyStatsRepository.findByDay(any(Date.class))).thenReturn(Optional.of(existing));
        when(userRepository.countByCreatedAtBetween(any(Date.class), any(Date.class))).thenReturn(10L);
        when(productRepository.countByCreatedAtBetween(any(Date.class), any(Date.class))).thenReturn(7L);
        when(recipeRepository.countByCreatedAtBetween(any(Date.class), any(Date.class))).thenReturn(4L);

        statisticsService.recalcStatsForDay(testDate);

        ArgumentCaptor<DailyStats> captor = ArgumentCaptor.forClass(DailyStats.class);
        verify(dailyStatsRepository, times(1)).save(captor.capture());

        DailyStats saved = captor.getValue();
        assertEquals(1L, saved.getId());
        assertEquals(10L, saved.getNewUsersCount());
        assertEquals(7L, saved.getNewProductsCount());
        assertEquals(4L, saved.getNewRecipesCount());
    }

    @Test
    void cleanupOldStats_deletesStatsOlderThan365Days() {
        statisticsService.cleanupOldStats();

        ArgumentCaptor<Date> captor = ArgumentCaptor.forClass(Date.class);
        verify(dailyStatsRepository, times(1)).deleteByDayBefore(captor.capture());

        Date borderDate = captor.getValue();
        assertNotNull(borderDate);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -365);

        // Check that the border date is approximately 365 days ago (within 1 day tolerance)
        long diff = Math.abs(borderDate.getTime() - cal.getTimeInMillis());
        assertTrue(diff < 86400000); // Less than 1 day in milliseconds
    }

    @Test
    void getYesterdayStats_returnsStatsForYesterday() {
        DailyStats yesterdayStats = DailyStats.builder()
                .id(1L)
                .newUsersCount(5L)
                .newProductsCount(3L)
                .newRecipesCount(2L)
                .build();

        when(dailyStatsRepository.findByDay(any(Date.class))).thenReturn(Optional.of(yesterdayStats));

        DailyStats result = statisticsService.getYesterdayStats();

        assertNotNull(result);
        assertEquals(5L, result.getNewUsersCount());
        assertEquals(3L, result.getNewProductsCount());
        assertEquals(2L, result.getNewRecipesCount());
    }

    @Test
    void getYesterdayStats_returnsNull_whenNoStats() {
        when(dailyStatsRepository.findByDay(any(Date.class))).thenReturn(Optional.empty());

        DailyStats result = statisticsService.getYesterdayStats();

        assertNull(result);
    }

    @Test
    void getStatsForDay_returnsStats_whenExists() {
        DailyStats stats = DailyStats.builder()
                .id(1L)
                .day(testDate)
                .newUsersCount(10L)
                .newProductsCount(5L)
                .newRecipesCount(3L)
                .build();

        when(dailyStatsRepository.findByDay(any(Date.class))).thenReturn(Optional.of(stats));

        DailyStats result = statisticsService.getStatsForDay(testDate);

        assertNotNull(result);
        assertEquals(10L, result.getNewUsersCount());
        assertEquals(5L, result.getNewProductsCount());
        assertEquals(3L, result.getNewRecipesCount());
    }

    @Test
    void getStatsForDay_returnsNull_whenNotExists() {
        when(dailyStatsRepository.findByDay(any(Date.class))).thenReturn(Optional.empty());

        DailyStats result = statisticsService.getStatsForDay(testDate);

        assertNull(result);
    }

    @Test
    void getAllStats_returnsAllStatsOrderedByDayDesc() {
        List<DailyStats> allStats = List.of(
                DailyStats.builder().id(1L).newUsersCount(5L).build(),
                DailyStats.builder().id(2L).newUsersCount(3L).build()
        );

        when(dailyStatsRepository.findAllByOrderByDayDesc()).thenReturn(allStats);

        List<DailyStats> result = statisticsService.getAllStats();

        assertEquals(2, result.size());
        assertEquals(5L, result.get(0).getNewUsersCount());
        assertEquals(3L, result.get(1).getNewUsersCount());
        verify(dailyStatsRepository, times(1)).findAllByOrderByDayDesc();
    }
}

