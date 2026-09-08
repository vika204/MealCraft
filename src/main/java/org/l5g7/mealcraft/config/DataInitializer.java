package org.l5g7.mealcraft.config;

import org.l5g7.mealcraft.app.mealplan.internal.MealPlan;
import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
import org.l5g7.mealcraft.app.products.Product;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredient;
import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredientRepository;
import org.l5g7.mealcraft.app.recipes.Recipe;
import org.l5g7.mealcraft.app.recipes.RecipeRepository;
import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItem;
import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItemRepository;
import org.l5g7.mealcraft.app.units.Unit;
import org.l5g7.mealcraft.app.units.UnitRepository;
import org.l5g7.mealcraft.app.user.PasswordHasher;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.l5g7.mealcraft.app.statistics.internal.DailyStats;
import org.l5g7.mealcraft.app.statistics.internal.DailyStatsRepository;
import org.l5g7.mealcraft.enums.MealPlanColor;
import org.l5g7.mealcraft.enums.MealStatus;
import org.l5g7.mealcraft.enums.Role;
import org.l5g7.mealcraft.logging.LogUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   UnitRepository unitRepository,
                                   ProductRepository productRepository,
                                   RecipeRepository recipeRepository,
                                   MealPlanRepository mealPlanRepository,
                                   ShoppingItemRepository shoppingItemRepository,
                                   RecipeIngredientRepository recipeIngredientRepository,
                                   DailyStatsRepository dailyStatsRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                PasswordHasher encoder = new PasswordHasher();

                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.DATE, -1);
                Date yesterday = cal.getTime();

                User admin = User.builder()
                        .username("admin")
                        .email("admin@mealcraft.org")
                        .password(encoder.hashPassword("admin123"))
                        .role(Role.ADMIN)
                        .avatarUrl("https://images.unsplash.com/photo-1495745966610-2a67f2297e5e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8M3x8cGhvdG9ncmFwaGVyfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000")
                        .createdAt(now)
                        .build();

                User user = User.builder()
                        .username("user")
                        .email("user@mealcraft.org")
                        .password(encoder.hashPassword("user123"))
                        .role(Role.USER)
                        .avatarUrl(null)
                        .createdAt(now)
                        .build();

                User premiumUser = User.builder()
                        .username("ira")
                        .email("ira@mealcraft.org")
                        .password(encoder.hashPassword("ira123"))
                        .role(Role.PREMIUM_USER)
                        .avatarUrl(null)
                        .createdAt(yesterday)
                        .build();

                userRepository.save(admin);
                userRepository.save(user);
                userRepository.save(premiumUser);

                LogUtils.logInfo("Default users created: admin & user");
                LogUtils.logInfo(user.getId() + "");

                Unit unit1 = Unit.builder()
                        .name("liter")
                        .build();

                Unit unit2 = Unit.builder()
                        .name("pc")
                        .build();

                Product product1 = Product.builder()
                        .name("Milk")
                        .imageUrl(null)
                        .defaultUnit(unit1)
                        .createdAt(yesterday)
                        .build();

                Product product2 = Product.builder()
                        .name("Egg")
                        .imageUrl(null)
                        .defaultUnit(unit2)
                        .createdAt(yesterday)
                        .build();

                Product product3 = Product.builder()
                        .name("Bread")
                        .imageUrl(null)
                        .defaultUnit(unit2)
                        .createdAt(yesterday)
                        .build();

                Product beetroot = Product.builder()
                        .name("Beetroot")
                        .imageUrl(null)
                        .defaultUnit(unit2)
                        .createdAt(yesterday)
                        .build();

                Product water = Product.builder()
                        .name("Water")
                        .imageUrl(null)
                        .defaultUnit(unit1)
                        .createdAt(yesterday)
                        .build();

                Product potato = Product.builder()
                        .name("Potato")
                        .imageUrl(null)
                        .defaultUnit(unit2)
                        .createdAt(yesterday)
                        .build();

                unitRepository.save(unit1);
                unitRepository.save(unit2);
                productRepository.save(product1);
                productRepository.save(product2);
                productRepository.save(product3);
                productRepository.save(beetroot);
                productRepository.save(water);
                productRepository.save(potato);


                Product userOnlyProduct1 = Product.builder()
                        .name("User secret cheese")
                        .imageUrl(null)
                        .defaultUnit(unit2)
                        .ownerUser(user)
                        .createdAt(now)
                        .build();

                Product userOnlyProduct2 = Product.builder()
                        .name("Ira private butter")
                        .imageUrl(null)
                        .defaultUnit(unit2)
                        .ownerUser(premiumUser)
                        .createdAt(now)
                        .build();

                productRepository.save(userOnlyProduct1);
                productRepository.save(userOnlyProduct2);

                Recipe userRecipe1 = Recipe.builder()
                        .name("User's secret sandwich")
                        .ownerUser(user)
                        .imageUrl(null)
                        .baseRecipe(null)
                        .createdAt(now)
                        .build();

                Recipe userRecipe2 = Recipe.builder()
                        .name("Ira's private toast")
                        .ownerUser(premiumUser)
                        .imageUrl(null)
                        .baseRecipe(null)
                        .createdAt(now)
                        .build();

                RecipeIngredient recipeIngredientU1 = RecipeIngredient.builder()
                        .product(product3)
                        .recipe(userRecipe1)
                        .amount(1d)
                        .build();

                RecipeIngredient recipeIngredientU12 = RecipeIngredient.builder()
                        .product(userOnlyProduct1)
                        .recipe(userRecipe1)
                        .amount(1d)
                        .build();


                RecipeIngredient recipeIngredientU2 = RecipeIngredient.builder()
                        .product(product3)
                        .recipe(userRecipe2)
                        .amount(1d)
                        .build();

                RecipeIngredient recipeIngredientU22 = RecipeIngredient.builder()
                        .product(userOnlyProduct2)
                        .recipe(userRecipe2)
                        .amount(1d)
                        .build();

                userRecipe1.setIngredients(List.of(recipeIngredientU1, recipeIngredientU12));
                userRecipe2.setIngredients(List.of(recipeIngredientU2, recipeIngredientU22));

                recipeRepository.save(userRecipe1);
                recipeRepository.save(userRecipe2);

                Recipe baseRecipe = Recipe.builder()
                        .name("Base Soup")
                        .createdAt(new Date())
                        .ownerUser(null)
                        .ingredients(List.of())
                        .build();

                Recipe recipe1 = Recipe.builder()
                        .name("Borshch")
                        .createdAt(new Date())
                        .ownerUser(user)
                        .baseRecipe(baseRecipe)
                        .imageUrl("https://example.com/borshch.jpg")
                        .build();

                RecipeIngredient recipeIngredient1 = RecipeIngredient.builder()
                        .product(beetroot)
                        .recipe(recipe1)
                        .amount(1d)
                        .build();

                RecipeIngredient recipeIngredient2 = RecipeIngredient.builder()
                        .product(potato)
                        .recipe(recipe1)
                        .amount(1d)
                        .build();

                RecipeIngredient recipeIngredient3 = RecipeIngredient.builder()
                        .product(product3)
                        .recipe(recipe1)
                        .amount(1d)
                        .build();

                Recipe recipe2 = Recipe.builder()
                        .name("Stewed beetroot")
                        .createdAt(new Date())
                        .ownerUser(null)
                        .baseRecipe(null)
                        .imageUrl(null)
                        .build();

                RecipeIngredient recipeIngredient21 = RecipeIngredient.builder()
                        .product(beetroot)
                        .recipe(recipe2)
                        .amount(1d)
                        .build();

                RecipeIngredient baseIngredient = RecipeIngredient.builder()
                        .product(water)
                        .recipe(baseRecipe)
                        .amount(1d)
                        .build();

                baseRecipe.setIngredients(List.of(baseIngredient));
                recipe1.setIngredients(List.of(recipeIngredient1, recipeIngredient2, recipeIngredient3));
                recipe2.setIngredients(List.of(recipeIngredient21));

                LocalDate localPlanDate1 = LocalDate.of(2025, 12, 3);
                LocalDate localPlanDate2 = LocalDate.of(2025, 12, 5);
                Date planDate1 = Date.from(localPlanDate1.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date planDate2 = Date.from(localPlanDate2.atStartOfDay(ZoneId.systemDefault()).toInstant());

                recipeRepository.save(baseRecipe);
                recipeRepository.save(recipe1);
                recipeRepository.save(recipe2);

                MealPlan mealPlan1 = MealPlan.builder()
                        .userOwner(user)
                        .recipe(recipe1)
                        .planDate(planDate1)
                        .servings(4)
                        .status(MealStatus.PLANNED)
                        .color(MealPlanColor.BLUE)
                        .build();

                MealPlan mealPlan2 = MealPlan.builder()
                        .userOwner(user)
                        .recipe(recipe2)
                        .planDate(planDate2)
                        .servings(12)
                        .status(MealStatus.PLANNED)
                        .color(MealPlanColor.ORANGE)
                        .build();

                mealPlanRepository.save(mealPlan1);
                mealPlanRepository.save(mealPlan2);

                for (RecipeIngredient ingredient : mealPlan1.getRecipe().getIngredients()) {
                    shoppingItemRepository.save(new ShoppingItem(null, mealPlan1.getUserOwner(), ingredient.getProduct(), ingredient.getAmount(), false, null));
                }

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(yesterday);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                Date from = startCal.getTime();

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(yesterday);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                Date to = endCal.getTime();

                long newUsersCount = userRepository.countByCreatedAtBetween(from, to);
                long newProductsCount = productRepository.countByCreatedAtBetween(from, to);
                long newRecipesCount = recipeRepository.countByCreatedAtBetween(from, to);

                DailyStats stats = dailyStatsRepository.findByDay(from).orElse(DailyStats.builder().day(from).build());
                stats.setNewUsersCount(newUsersCount);
                stats.setNewProductsCount(newProductsCount);
                stats.setNewRecipesCount(newRecipesCount);
                dailyStatsRepository.save(stats);
            }
        };
    }
}
