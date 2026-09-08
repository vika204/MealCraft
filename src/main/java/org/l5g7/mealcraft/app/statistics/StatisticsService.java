package org.l5g7.mealcraft.app.statistics;

import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.recipes.RecipeRepository;
import org.l5g7.mealcraft.app.statistics.internal.DailyStats;
import org.l5g7.mealcraft.app.statistics.internal.DailyStatsRepository;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class StatisticsService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;
    private final DailyStatsRepository dailyStatsRepository;

    @Autowired
    public StatisticsService(UserRepository userRepository,
                             ProductRepository productRepository,
                             RecipeRepository recipeRepository,
                             DailyStatsRepository dailyStatsRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.recipeRepository = recipeRepository;
        this.dailyStatsRepository = dailyStatsRepository;
    }

    public void recalcStatsForDay(Date targetDay) {
        Date from = startOfDay(targetDay);
        Date to = endOfDay(targetDay);

        long newUsers = userRepository.countByCreatedAtBetween(from, to);
        long newProducts = productRepository.countByCreatedAtBetween(from, to);
        long newRecipes = recipeRepository.countByCreatedAtBetween(from, to);

        Date dayOnly = stripTime(targetDay);

        Optional<DailyStats> existing = dailyStatsRepository.findByDay(dayOnly);

        DailyStats stats;
        if (existing.isPresent()) {
            stats = existing.get();
        } else {
            stats = new DailyStats();
            stats.setDay(dayOnly);
        }

        stats.setNewUsersCount(newUsers);
        stats.setNewProductsCount(newProducts);
        stats.setNewRecipesCount(newRecipes);

        dailyStatsRepository.save(stats);
    }

    public void cleanupOldStats() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -365);
        Date border = stripTime(cal.getTime());
        dailyStatsRepository.deleteByDayBefore(border);
    }

    public DailyStats getYesterdayStats() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date dayOnly = stripTime(cal.getTime());
        return getStatsForDay(dayOnly);
    }

    public DailyStats getStatsForDay(Date date) {
        Date dayOnly = stripTime(date);
        Optional<DailyStats> existing = dailyStatsRepository.findByDay(dayOnly);
        return existing.orElse(null);
    }

    public List<DailyStats> getAllStats() {
        return dailyStatsRepository.findAllByOrderByDayDesc();
    }

    private Date stripTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date startOfDay(Date date) {
        return stripTime(date);
    }

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(stripTime(date));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}