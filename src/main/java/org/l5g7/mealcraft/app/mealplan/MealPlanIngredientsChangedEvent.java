package org.l5g7.mealcraft.app.mealplan;

// подія, яка повідомляє про зміну кількості інгредієнта в MealPlan.
public record MealPlanIngredientsChangedEvent(
        Long userId,
        Long productId,
        String productName,
        Double quantityDelta
) {
}