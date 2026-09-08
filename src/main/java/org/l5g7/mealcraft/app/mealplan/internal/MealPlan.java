package org.l5g7.mealcraft.app.mealplan.internal;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.l5g7.mealcraft.app.recipes.Recipe;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.enums.MealPlanColor;
import org.l5g7.mealcraft.enums.MealStatus;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User userOwner;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Recipe recipe;

    @Temporal(TemporalType.TIMESTAMP)
    private Date planDate;

    @Column(nullable = false)
    @Min(value = 1, message = "MealPlan servings must be > 0")
    private Integer servings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MealPlanColor color;

}
