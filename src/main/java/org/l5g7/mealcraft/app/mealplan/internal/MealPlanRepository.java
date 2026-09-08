package org.l5g7.mealcraft.app.mealplan.internal;

import org.l5g7.mealcraft.app.recipes.Recipe;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.enums.MealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findAllByUserOwner(User userOwner);
    List<MealPlan> findAllByUserOwnerAndPlanDateBetween(User user, Date from, Date to);
    List<MealPlan> findMealPlanByUserOwnerAndStatusAndPlanDateBetween(User userOwner, MealStatus status, Date from, Date to);
    List<MealPlan> findMealPlanByUserOwnerAndStatusNotAndPlanDateBetween(User userOwner, MealStatus notStatus, Date from, Date to);
    List<MealPlan> findAllByStatusAndPlanDateBetween(MealStatus status, Date from, Date to);
    boolean existsByRecipe(Recipe recipe);
}
