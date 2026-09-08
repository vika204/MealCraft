package org.l5g7.mealcraft.unittest.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.l5g7.mealcraft.app.mealplan.internal.MealPlan;
import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
import org.l5g7.mealcraft.app.notification.MealExpirationNotificationService;
import org.l5g7.mealcraft.app.notification.NotificationService;
import org.l5g7.mealcraft.app.recipes.Recipe;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.enums.MealStatus;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealExpirationNotificationServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MealExpirationNotificationService expirationService;

    private User user;
    private Recipe recipe;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        recipe = new Recipe();
        recipe.setId(10L);
        recipe.setName("Pasta");
    }

    @Test
    void notifyForDay_sendsNotification_whenExpirationMatchesToday() {
        Calendar cal = Calendar.getInstance();
        Date planDate = cal.getTime();

        MealPlan plan = new MealPlan();
        plan.setPlanDate(planDate);
        plan.setServings(1); // expiration today
        plan.setStatus(MealStatus.PLANNED);
        plan.setUserOwner(user);
        plan.setRecipe(recipe);

        when(mealPlanRepository.findAllByStatusAndPlanDateBetween(eq(MealStatus.PLANNED), any(Date.class), any(Date.class)))
                .thenReturn(List.of(plan));

        Date today = new Date();
        expirationService.notifyForDay(today);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService, times(1)).createNotification(eq(user), textCaptor.capture());
        String text = textCaptor.getValue();
        assertTrue(text.contains("Pasta"));
        assertTrue(text.contains("expires today"));
    }

    @Test
    void notifyForDay_skips_whenExpirationNotToday() {
        MealPlan plan = new MealPlan();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -5);
        plan.setPlanDate(cal.getTime());
        plan.setServings(10); // expiration in future
        plan.setStatus(MealStatus.PLANNED);
        plan.setUserOwner(user);
        plan.setRecipe(recipe);

        when(mealPlanRepository.findAllByStatusAndPlanDateBetween(eq(MealStatus.PLANNED), any(Date.class), any(Date.class)))
                .thenReturn(List.of(plan));

        expirationService.notifyForDay(new Date());
        verify(notificationService, never()).createNotification(any(User.class), anyString());
    }
}

