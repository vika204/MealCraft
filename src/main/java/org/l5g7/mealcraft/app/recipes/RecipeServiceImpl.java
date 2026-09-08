package org.l5g7.mealcraft.app.recipes;

import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
import org.l5g7.mealcraft.app.products.Product;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.products.ProductService;
import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredient;
import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredientDto;
import org.l5g7.mealcraft.app.units.Unit;
import org.l5g7.mealcraft.app.units.UnitService;
import org.l5g7.mealcraft.app.user.CurrentUserProvider;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.l5g7.mealcraft.exception.EntityDoesNotExistException;
import org.l5g7.mealcraft.mealcraftstarterexternalrecipes.ExternalRecipe;
import org.l5g7.mealcraft.mealcraftstarterexternalrecipes.RecipeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RecipeProvider recipeProvider;
    private final CurrentUserProvider currentUserProvider;
    private final UnitService unitService;
    private final ProductService productService;
    private final MealPlanRepository mealPlanRepository;

    private static final String ENTITY_NAME = "Recipe";
    private static final String ENTITY_PRODUCT = "Product";

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository, ProductRepository productRepository, UserRepository userRepository, RecipeProvider recipeProvider, CurrentUserProvider currentUserProvider, UnitService unitService, ProductService productService, MealPlanRepository mealPlanRepository) {
        this.recipeRepository = recipeRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.recipeProvider = recipeProvider;
        this.currentUserProvider = currentUserProvider;
        this.unitService = unitService;
        this.productService = productService;
        this.mealPlanRepository = mealPlanRepository;
    }

    @Transactional
    @Override
    public List<RecipeDto> getAllRecipes() {
        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();

        List<Recipe> entities;

        if (currentUser == null) {
            entities = recipeRepository.findAllByOwnerUserIsNull();
        } else {
            entities = recipeRepository.findAllByOwnerUserIsNullOrOwnerUser_Id(currentUser.getId());
        }

        return entities.stream()
                .map(entity -> {
                    List<RecipeIngredientDto> ingredients = (entity.getIngredients() != null)
                            ? entity.getIngredients().stream()
                            .map(ingredient -> RecipeIngredientDto.builder()
                                    .id(ingredient.getId())
                                    .productId(ingredient.getProduct().getId())
                                    .productName(ingredient.getProduct().getName())
                                    .amount(ingredient.getAmount())
                                    .build())
                            .toList()
                            : List.of();

                    return RecipeDto.builder()
                            .id(entity.getId())
                            .name(entity.getName())
                            .ownerUserId(entity.getOwnerUser() != null ? entity.getOwnerUser().getId() : null)
                            .baseRecipeId(entity.getBaseRecipe() != null ? entity.getBaseRecipe().getId() : null)
                            .createdAt(entity.getCreatedAt())
                            .imageUrl(entity.getImageUrl())
                            .ingredients(ingredients)
                            .build();
                }).toList();
    }

    @Transactional
    @Override
    public RecipeDto getRecipeById(Long id) {
        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();

        Optional<Recipe> recipe = recipeRepository.findById(id);

        if (recipe.isPresent()) {
            Recipe entity = recipe.get();

            User owner = entity.getOwnerUser();
            if (owner != null && (currentUser == null || !owner.getId().equals(currentUser.getId()))) {
                throw new EntityDoesNotExistException(ENTITY_NAME, "id", String.valueOf(id));
            }

            List<RecipeIngredientDto> ingredients = entity.getIngredients() != null
                    ? entity.getIngredients().stream()
                    .map(ingredient -> RecipeIngredientDto.builder()
                            .id(ingredient.getId())
                            .productId(ingredient.getProduct().getId())
                            .productName(ingredient.getProduct().getName())
                            .amount(ingredient.getAmount())
                            .build())
                    .toList()
                    : List.of();

            return new RecipeDto(
                    entity.getId(),
                    entity.getName(),
                    owner != null ? owner.getId() : null,
                    entity.getBaseRecipe() != null ? entity.getBaseRecipe().getId() : null,
                    entity.getCreatedAt(),
                    entity.getImageUrl(),
                    ingredients
            );
        } else {
            throw new EntityDoesNotExistException(ENTITY_NAME, "id", String.valueOf(id));
        }
    }

    @Transactional
    @Override
    public void createRecipe(RecipeDto recipeDto) {

        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();

        if (recipeDto.getIngredients() == null || recipeDto.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must contain at least one ingredient");
        }

        Recipe baseRecipe = null;
        if (recipeDto.getBaseRecipeId() != null) {
            baseRecipe = recipeRepository.findById(recipeDto.getBaseRecipeId())
                    .orElseThrow(() -> new EntityDoesNotExistException(
                            ENTITY_NAME,
                            "id",
                            String.valueOf(recipeDto.getBaseRecipeId())
                    ));
        }

        Recipe entity = Recipe.builder()
                .name(recipeDto.getName())
                .ownerUser(currentUser)
                .imageUrl(recipeDto.getImageUrl())
                .baseRecipe(baseRecipe)
                .createdAt(new Date())
                .build();

        List<RecipeIngredient> ingredients = new ArrayList<>();

        Set<Long> usedProducts = new HashSet<>();
        for (RecipeIngredientDto ingDto : recipeDto.getIngredients()) {
            Long productId = ingDto.getProductId();
            if (productId == null) {
                throw new IllegalArgumentException("Product must be selected for each ingredient");
            }

            if (!usedProducts.add(productId)) {
                throw new IllegalArgumentException("Recipe cannot contain the same product more than once");
            }

            if (ingDto.getAmount() == null || ingDto.getAmount().doubleValue() <= 0) {
                throw new IllegalArgumentException("Ingredient amount must be positive");
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityDoesNotExistException(
                            ENTITY_PRODUCT,
                            "id",
                            String.valueOf(productId)
                    ));

            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .recipe(entity)
                    .product(product)
                    .amount(ingDto.getAmount())
                    .build();

            ingredients.add(ingredient);
        }

        entity.setIngredients(ingredients);

        recipeRepository.save(entity);
    }

    @Transactional
    @Override
    public void updateRecipe(Long id, RecipeDto recipeDto) {
        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityDoesNotExistException(
                        ENTITY_NAME,
                        "id",
                        String.valueOf(id)
                ));

        if (recipeDto.getIngredients() == null || recipeDto.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must contain at least one ingredient");
        }

        User user;
        if (recipeDto.getOwnerUserId() != null) {
            user = userRepository.findById(recipeDto.getOwnerUserId())
                    .orElseThrow(() -> new EntityDoesNotExistException(
                            "User",
                            "id",
                            String.valueOf(recipeDto.getOwnerUserId())
                    ));
        } else {
            user = null;
        }

        Recipe baseRecipe;
        if (recipeDto.getBaseRecipeId() != null) {
            baseRecipe = recipeRepository.findById(recipeDto.getBaseRecipeId())
                    .orElseThrow(() -> new EntityDoesNotExistException(
                            ENTITY_NAME,
                            "id",
                            String.valueOf(recipeDto.getBaseRecipeId())
                    ));
        } else {
            baseRecipe = null;
        }

        User owner = recipe.getOwnerUser();
        verifyUserSituation(owner, currentUser, id);

        recipe.setName(recipeDto.getName());
        recipe.setOwnerUser(user);
        recipe.setImageUrl(recipeDto.getImageUrl());
        recipe.setBaseRecipe(baseRecipe);

        List<RecipeIngredient> ingredients = new ArrayList<>();

        Set<Long> usedProducts = new HashSet<>();

        for (RecipeIngredientDto ingDto : recipeDto.getIngredients()) {
            Long productId = ingDto.getProductId();
            verifyIngredients(ingDto, usedProducts);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityDoesNotExistException(
                            ENTITY_PRODUCT,
                            "id",
                            String.valueOf(productId)
                    ));

            User productOwner = product.getOwnerUser();
            verifyCurrentUserAndOwner(productOwner, currentUser, id);

            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .recipe(recipe)
                    .product(product)
                    .amount(ingDto.getAmount())
                    .build();

            ingredients.add(ingredient);
        }

        recipe.getIngredients().clear();
        recipe.getIngredients().addAll(ingredients);

        recipeRepository.save(recipe);
    }

    private void verifyIngredients(RecipeIngredientDto ingDto, Set<Long> usedProducts) {
        Long productId = ingDto.getProductId();
        if (productId == null) {
            throw new IllegalArgumentException("Product must be selected for each ingredient");
        }

        if (!usedProducts.add(productId)) {
            throw new IllegalArgumentException("Recipe cannot contain the same product more than once");
        }

        if (ingDto.getAmount() == null || ingDto.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("Ingredient amount must be positive");
        }
    }

    private void verifyUserSituation(User owner, User currentUser, Long id) {
        if (owner == null) {
            if (currentUser != null) {
                throw new EntityDoesNotExistException(ENTITY_NAME, "id", String.valueOf(id));
            }
        } else {
            if (currentUser == null || !owner.getId().equals(currentUser.getId())) {
                throw new EntityDoesNotExistException(ENTITY_NAME, "id", String.valueOf(id));
            }
        }
    }

    @Transactional
    @Override
    public void patchRecipe(Long id, RecipeDto patch) {
        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityDoesNotExistException(
                        ENTITY_NAME,
                        "id",
                        String.valueOf(id)
                ));

        User owner = recipe.getOwnerUser();
        verifyUserSituation(owner, currentUser, id);

        if (patch.getName() != null) {
            recipe.setName(patch.getName());
        }

        if (patch.getImageUrl() != null) {
            recipe.setImageUrl(patch.getImageUrl());
        }

        if (patch.getIngredients() != null) {

            if (patch.getIngredients().isEmpty()) {
                throw new IllegalArgumentException("Recipe must contain at least one ingredient");
            }

            List<RecipeIngredient> ingredients = new ArrayList<>();
            Set<Long> usedProducts = new HashSet<>();

            for (RecipeIngredientDto ingDto : patch.getIngredients()) {
                if (ingDto == null) {
                    continue;
                }

                Long productId = ingDto.getProductId();
                verifyIngredients(ingDto, usedProducts);

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new EntityDoesNotExistException(
                                ENTITY_PRODUCT,
                                "id",
                                String.valueOf(productId)
                        ));

                User productOwner = product.getOwnerUser();
                verifyCurrentUserAndOwner(productOwner,currentUser,productId);

                RecipeIngredient ingredient = RecipeIngredient.builder()
                        .recipe(recipe)
                        .product(product)
                        .amount(ingDto.getAmount())
                        .build();

                ingredients.add(ingredient);
            }

            recipe.getIngredients().clear();
            recipe.getIngredients().addAll(ingredients);
        }

        if (patch.getOwnerUserId() != null) {
            recipe.setOwnerUser(userRepository.findById(patch.getOwnerUserId())
                    .orElseThrow(() -> new EntityDoesNotExistException(
                            "User",
                            "id",
                            String.valueOf(patch.getOwnerUserId())
                    )));
        }

        if (patch.getBaseRecipeId() != null) {
            recipe.setBaseRecipe(recipeRepository.findById(patch.getBaseRecipeId())
                    .orElseThrow(() -> new EntityDoesNotExistException(
                            ENTITY_NAME,
                            "id",
                            String.valueOf(patch.getBaseRecipeId())
                    )));
        }
        recipeRepository.save(recipe);
    }

    private void verifyCurrentUserAndOwner(User productOwner, User currentUser, Long productId) {
        if (productOwner != null && (currentUser == null || !productOwner.getId().equals(currentUser.getId()))) {
            throw new EntityDoesNotExistException(
                    ENTITY_PRODUCT,
                    "id",
                    String.valueOf(productId)
            );
        }
    }

    @Transactional
    @Override
    public void deleteRecipeById(Long id) {

        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityDoesNotExistException(
                        ENTITY_NAME,
                        "id",
                        String.valueOf(id)
                ));

        User owner = recipe.getOwnerUser();

        if (owner == null) {
            if (currentUser != null) {
                throw new EntityDoesNotExistException(ENTITY_NAME, "id", String.valueOf(id));
            }
        } else {
            if (currentUser == null || !owner.getId().equals(currentUser.getId())) {
                throw new EntityDoesNotExistException(ENTITY_NAME, "id", String.valueOf(id));
            }
        }

        if (mealPlanRepository.existsByRecipe(recipe)) {
            throw new IllegalStateException("Cannot delete recipe because it is used in a meal plan");
        }

        List<Recipe> children = recipeRepository.findAllByBaseRecipe(recipe);
        if (!children.isEmpty()) {
            for (Recipe child : children) {
                child.setBaseRecipe(null);
            }
            recipeRepository.saveAll(children);
        }

        recipeRepository.deleteById(id);
    }


    @Transactional
    @Override
    public RecipeDto getRandomRecipe() throws NoSuchElementException {
        ExternalRecipe externalRecipe = recipeProvider.getRandomRecipe();

        return RecipeDto.builder()
                .id(externalRecipe.id())
                .name(externalRecipe.name())
                .imageUrl(externalRecipe.imageUrl())
                .build();
    }

    @Transactional
    @Override
    public List<RecipeDto> getRecipesByProducts(List<String> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();

        Set<String> allowedProductNames = products.stream()
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());

        List<Recipe> entities;

        if (currentUser == null) {
            entities = recipeRepository.findAllByOwnerUserIsNull();
        } else {
            entities = recipeRepository.findAllByOwnerUserIsNullOrOwnerUser_Id(currentUser.getId());
        }

        List<Recipe> filteredEntities = entities.stream()
                .filter(recipe -> {
                    if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
                        return false;
                    }

                    return recipe.getIngredients().stream()
                            .allMatch(ingredient ->
                                ingredient.getProduct() != null &&
                                allowedProductNames.contains(ingredient.getProduct().getName().toLowerCase())
                            );
                })
                .toList();

        return filteredEntities.stream()
                .map(entity -> {
                    List<RecipeIngredientDto> ingredients = (entity.getIngredients() != null)
                            ? entity.getIngredients().stream()
                            .map(ingredient -> RecipeIngredientDto.builder()
                                    .id(ingredient.getId())
                                    .productId(ingredient.getProduct().getId())
                                    .productName(ingredient.getProduct().getName())
                                    .amount(ingredient.getAmount())
                                    .build())
                            .toList()
                            : List.of();

                    return RecipeDto.builder()
                            .id(entity.getId())
                            .name(entity.getName())
                            .ownerUserId(entity.getOwnerUser() != null ? entity.getOwnerUser().getId() : null)
                            .baseRecipeId(entity.getBaseRecipe() != null ? entity.getBaseRecipe().getId() : null)
                            .createdAt(entity.getCreatedAt())
                            .imageUrl(entity.getImageUrl())
                            .ingredients(ingredients)
                            .build();
                }).toList();
    }

    @Override
    @Transactional
    public void importRecipe(RecipeDto dto) {
        validateImportUser();
        validateImportHasIngredients(dto);

        Recipe entity = buildImportedRecipe(dto);
        List<RecipeIngredient> ingredients = buildImportedIngredients(dto.getIngredients(), entity);

        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Imported recipe must contain at least one ingredient");
        }

        entity.setIngredients(ingredients);
        recipeRepository.save(entity);
    }

    private void validateImportUser() {
        User currentUser = currentUserProvider.getCurrentUserOrNullIfAdmin();
        if (currentUser != null) {
            throw new EntityDoesNotExistException(ENTITY_NAME, "id", "import-not-allowed");
        }
    }

    private void validateImportHasIngredients(RecipeDto dto) {
        if (dto.getIngredients() == null || dto.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Imported recipe must contain at least one ingredient");
        }
    }

    private Recipe buildImportedRecipe(RecipeDto dto) {
        return Recipe.builder()
                .name(dto.getName())
                .ownerUser(null)
                .imageUrl(dto.getImageUrl())
                .baseRecipe(null)
                .createdAt(new Date())
                .build();
    }

    private List<RecipeIngredient> buildImportedIngredients(List<RecipeIngredientDto> ingredientDtos,
                                                            Recipe recipe) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Set<String> usedProductNames = new java.util.HashSet<>();

        for (RecipeIngredientDto ingDto : ingredientDtos) {
            if (isCompletelyEmpty(ingDto)) {
                continue;
            }

            String productName = validateAndNormalizeName(ingDto.getProductName());
            Double amount = validateAndGetAmount(ingDto.getAmount());
            validateUniqueProduct(usedProductNames, productName);

            String unitName = normalizeUnitName(ingDto.getUnitName());
            Unit unit = unitService.getOrCreateUnitByName(unitName);
            Product product = productService.getOrCreatePublicProduct(productName, unit);

            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .recipe(recipe)
                    .product(product)
                    .amount(amount)
                    .build();

            ingredients.add(ingredient);
        }

        return ingredients;
    }

    private boolean isCompletelyEmpty(RecipeIngredientDto ingDto) {
        if (ingDto == null) {
            return true;
        }
        String rawName = ingDto.getProductName();
        Double amount = ingDto.getAmount();
        return (rawName == null || rawName.isBlank())
                && (amount == null || amount <= 0);
    }

    private String validateAndNormalizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("Ingredient must have product name");
        }
        return rawName.trim();
    }

    private Double validateAndGetAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Ingredient amount must be positive");
        }
        return amount;
    }

    private void validateUniqueProduct(Set<String> usedProductNames, String productName) {
        String key = productName.toLowerCase();
        if (!usedProductNames.add(key)) {
            throw new IllegalArgumentException("Recipe cannot contain the same product more than once");
        }
    }

    private String normalizeUnitName(String unitName) {
        if (unitName == null || unitName.isBlank()) {
            return "pc";
        }
        return unitName.trim();
    }

}