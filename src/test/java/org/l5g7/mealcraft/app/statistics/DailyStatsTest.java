package org.l5g7.mealcraft.app.statistics;

import org.junit.jupiter.api.Test;
import org.l5g7.mealcraft.app.statistics.internal.DailyStats;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DailyStatsTest {

    @Test
    void dailyStats_builder_createsInstance() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JANUARY, 15, 0, 0, 0);
        Date testDate = cal.getTime();

        DailyStats stats = DailyStats.builder()
                .id(1L)
                .day(testDate)
                .newUsersCount(10L)
                .newProductsCount(5L)
                .newRecipesCount(3L)
                .build();

        assertNotNull(stats);
        assertEquals(1L, stats.getId());
        assertEquals(testDate, stats.getDay());
        assertEquals(10L, stats.getNewUsersCount());
        assertEquals(5L, stats.getNewProductsCount());
        assertEquals(3L, stats.getNewRecipesCount());
    }

    @Test
    void dailyStats_settersAndGetters_work() {
        DailyStats stats = new DailyStats();
        Calendar cal = Calendar.getInstance();
        Date testDate = cal.getTime();

        stats.setId(100L);
        stats.setDay(testDate);
        stats.setNewUsersCount(20L);
        stats.setNewProductsCount(15L);
        stats.setNewRecipesCount(10L);

        assertEquals(100L, stats.getId());
        assertEquals(testDate, stats.getDay());
        assertEquals(20L, stats.getNewUsersCount());
        assertEquals(15L, stats.getNewProductsCount());
        assertEquals(10L, stats.getNewRecipesCount());
    }

    @Test
    void dailyStats_noArgsConstructor_createsInstance() {
        DailyStats stats = new DailyStats();
        assertNotNull(stats);
    }

    @Test
    void dailyStats_allArgsConstructor_createsInstance() {
        Calendar cal = Calendar.getInstance();
        Date testDate = cal.getTime();

        DailyStats stats = new DailyStats(1L, testDate, 5L, 3L, 2L);

        assertEquals(1L, stats.getId());
        assertEquals(testDate, stats.getDay());
        assertEquals(5L, stats.getNewUsersCount());
        assertEquals(3L, stats.getNewProductsCount());
        assertEquals(2L, stats.getNewRecipesCount());
    }

    @Test
    void dailyStats_equals_worksCorrectly() {
        Calendar cal = Calendar.getInstance();
        Date testDate = cal.getTime();

        DailyStats stats1 = DailyStats.builder()
                .id(1L)
                .day(testDate)
                .newUsersCount(10L)
                .build();

        DailyStats stats2 = DailyStats.builder()
                .id(1L)
                .day(testDate)
                .newUsersCount(10L)
                .build();

        assertEquals(stats1, stats2);
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void dailyStats_toString_containsFields() {
        DailyStats stats = DailyStats.builder()
                .id(1L)
                .newUsersCount(10L)
                .newProductsCount(5L)
                .newRecipesCount(3L)
                .build();

        String str = stats.toString();
        assertTrue(str.contains("newUsersCount"));
        assertTrue(str.contains("10"));
    }
}

