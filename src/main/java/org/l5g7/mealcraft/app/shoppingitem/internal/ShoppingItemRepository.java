package org.l5g7.mealcraft.app.shoppingitem.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByUserOwnerId(Long userId);
    @Modifying
    @Transactional
    @Query("DELETE FROM ShoppingItem s WHERE s.status = true AND s.boughtAt < :time")
    void deleteBought(Date time);
}
