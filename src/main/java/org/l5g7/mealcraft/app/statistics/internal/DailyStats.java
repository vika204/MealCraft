package org.l5g7.mealcraft.app.statistics.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "stats_day", nullable = false, unique = true)
    private Date day;

    private long newUsersCount;

    private long newProductsCount;

    private long newRecipesCount;
}

