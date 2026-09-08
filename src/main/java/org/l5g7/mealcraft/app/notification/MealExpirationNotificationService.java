package org.l5g7.mealcraft.app.notification;

import org.l5g7.mealcraft.app.mealplan.internal.MealPlan;
import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
import org.l5g7.mealcraft.enums.MealStatus;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class MealExpirationNotificationService {

    private final MealPlanRepository mealPlanRepository;
    private final NotificationService notificationService;

    public MealExpirationNotificationService(MealPlanRepository mealPlanRepository, NotificationService notificationService) {
        this.mealPlanRepository = mealPlanRepository;
        this.notificationService = notificationService;
    }

    public void notifyForDay(Date today) {
        Date dayStart = stripTime(today);
        Date dayEnd = endOfDay(today);

        Calendar cal = Calendar.getInstance();
        cal.setTime(dayStart);
        cal.add(Calendar.DATE, -30);
        Date from = cal.getTime();

        List<MealPlan> plans = mealPlanRepository.findAllByStatusAndPlanDateBetween(MealStatus.PLANNED, from, dayEnd);

        for (MealPlan plan : plans) {
            Date expirationDate = calculateExpirationDate(plan.getPlanDate(), plan.getServings());
            if (isSameDay(expirationDate, dayStart)) {
                String text = buildExpirationText(plan);
                notificationService.createNotification(plan.getUserOwner(), text);
            }
        }
    }

    private Date calculateExpirationDate(Date planDate, Integer servings) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(stripTime(planDate));
        cal.add(Calendar.DATE, servings - 1);
        return cal.getTime();
    }

    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
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

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(stripTime(date));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private String buildExpirationText(MealPlan plan) {
        String recipeName = plan.getRecipe().getName();
        return "The meal \"" + recipeName + "\" expires today.";
    }

}
