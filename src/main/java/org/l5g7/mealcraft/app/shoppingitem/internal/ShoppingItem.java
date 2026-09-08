package org.l5g7.mealcraft.app.shoppingitem.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.l5g7.mealcraft.app.products.Product;
import org.l5g7.mealcraft.app.user.User;

import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShoppingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User userOwner;

    @ManyToOne
    private Product product;

    @Column(nullable = false)
    private Double requiredQty;

    @Column(nullable = false)
    private Boolean status;  // to-buy - false, bought - true

    @Temporal(TemporalType.TIMESTAMP)
    private Date boughtAt;
}
