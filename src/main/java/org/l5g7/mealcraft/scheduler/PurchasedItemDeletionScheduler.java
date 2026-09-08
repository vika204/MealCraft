package org.l5g7.mealcraft.scheduler;

import lombok.RequiredArgsConstructor;
import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItemRepository;
import org.l5g7.mealcraft.logging.LogUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class PurchasedItemDeletionScheduler {
    private final ShoppingItemRepository shoppingItemRepository;

    @Scheduled(fixedDelayString =  "#{${shopping-item-cleanup-tick-delay-minutes} * 60 * 1000}")
    public void cleanUpPurchasedItems() {
        Date twoMinutesAgo = new Date(System.currentTimeMillis() - 2 * 60 * 1000);
        shoppingItemRepository.deleteBought(twoMinutesAgo);
        LogUtils.logInfo("Deleted items purchased 2 minutes ago");
    }

}
