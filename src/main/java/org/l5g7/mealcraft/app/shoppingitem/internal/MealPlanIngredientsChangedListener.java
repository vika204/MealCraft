package org.l5g7.mealcraft.app.shoppingitem.internal;

import org.l5g7.mealcraft.app.mealplan.MealPlanIngredientsChangedEvent;
import org.l5g7.mealcraft.app.shoppingitem.ShoppingItemDto;
import org.l5g7.mealcraft.app.shoppingitem.ShoppingItemService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;


// слухач обробляє події MealPlanIngredientsChangedEvent і взємодіє з ShoppingItem через ShoppingItemService.
@Component
public class MealPlanIngredientsChangedListener {

    private final ShoppingItemService shoppingItemService;

    public MealPlanIngredientsChangedListener(ShoppingItemService shoppingItemService) {
        this.shoppingItemService = shoppingItemService;
    }

    @ApplicationModuleListener
    public void on(MealPlanIngredientsChangedEvent event) {

        if (event.quantityDelta() == 0) {
            return;
        }

        ShoppingItemDto shoppingItemDto = ShoppingItemDto.builder()
                .id(null)
                .name(event.productName())
                .userOwnerId(event.userId())
                .productId(event.productId())
                .requiredQty(Math.abs(event.quantityDelta()))
                .status(false)
                .unitName(null)
                .boughtAt(null)
                .build();

        if (event.quantityDelta() > 0) {
            shoppingItemService.addShoppingItem(shoppingItemDto);
        } else {
            shoppingItemService.removeShoppingItem(shoppingItemDto);
        }
    }
}