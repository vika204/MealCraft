package org.l5g7.mealcraft.app.statistics.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    Optional<DailyStats> findByDay(Date dayOnly);
    void deleteByDayBefore(Date day);
    List<DailyStats> findAllByOrderByDayDesc();
}
