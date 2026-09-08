package org.l5g7.mealcraft.unittest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
import org.l5g7.mealcraft.app.products.Product;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.products.ProductService;
import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredientDto;
import org.l5g7.mealcraft.app.recipes.Recipe;
import org.l5g7.mealcraft.app.recipes.RecipeDto;
import org.l5g7.mealcraft.app.recipes.RecipeRepository;
import org.l5g7.mealcraft.app.recipes.RecipeServiceImpl;
import org.l5g7.mealcraft.app.units.UnitService;
import org.l5g7.mealcraft.app.user.CurrentUserProvider;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.l5g7.mealcraft.exception.EntityDoesNotExistException;
import org.l5g7.mealcraft.mealcraftstarterexternalrecipes.RecipeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("java:S5786")
public class RecipeServiceUnitTests {

    private RecipeRepository recipeRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private RecipeProvider recipeProvider;
    private CurrentUserProvider currentUserProvider;
    private UnitService unitService;
    private ProductService productService;
    private MealPlanRepository mealPlanRepository;

    private RecipeServiceImpl recipeService;

    @BeforeEach
    void setUp() {
        recipeRepository = mock(RecipeRepository.class);
        productRepository = mock(ProductRepository.class);
        userRepository = mock(UserRepository.class);
        recipeProvider = mock(RecipeProvider.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        unitService = mock(UnitService.class);
        productService = mock(ProductService.class);
        mealPlanRepository = mock(MealPlanRepository.class);

        recipeService = new RecipeServiceImpl(
                recipeRepository,
                productRepository,
                userRepository,
                recipeProvider,
                currentUserProvider,
                unitService,
                productService,
                mealPlanRepository


        );
    }

    private void mockCurrentUser(User user) {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(user);
    }

    private void mockAdmin() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
    }

    @Test
    void createRecipe_withValidDto_savesRecipe() {
        User user = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(user);

        Product product = Product.builder()
                .id(10L)
                .name("Sugar")
                .ownerUser(user)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        RecipeIngredientDto ingDto = RecipeIngredientDto.builder()
                .productId(10L)
                .amount(2.5)
                .build();

        RecipeDto dto = RecipeDto.builder()
                .name("Pancakes")
                .imageUrl("pancakes.jpg")
                .ingredients(List.of(ingDto))
                .build();

        recipeService.createRecipe(dto);

        verify(recipeRepository, times(1)).save(argThat(recipe ->
                recipe.getName().equals("Pancakes")
                        && recipe.getOwnerUser() != null
                        && recipe.getOwnerUser().getId().equals(1L)
                        && recipe.getIngredients() != null
                        && recipe.getIngredients().size() == 1
                        && recipe.getIngredients().get(0).getProduct().equals(product)
                        && Double.compare(recipe.getIngredients().get(0).getAmount(), 2.5) == 0
        ));
    }

    @Test
    void createRecipe_withoutIngredients_throwsException() {
        User user = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(user);

        RecipeDto dto = RecipeDto.builder()
                .name("Empty recipe")
                .imageUrl("empty.jpg")
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.createRecipe(dto)
        );

        assertEquals("Recipe must contain at least one ingredient", ex.getMessage());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipe_withDuplicateProducts_throwsException() {
        User user = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(user);

        Product product = Product.builder()
                .id(10L)
                .name("Sugar")
                .ownerUser(user)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        RecipeIngredientDto ing1 = RecipeIngredientDto.builder()
                .productId(10L)
                .amount(1.0)
                .build();

        RecipeIngredientDto ing2 = RecipeIngredientDto.builder()
                .productId(10L)
                .amount(2.0)
                .build();

        RecipeDto dto = RecipeDto.builder()
                .name("Bad recipe")
                .ingredients(List.of(ing1, ing2))
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.createRecipe(dto)
        );

        assertEquals("Recipe cannot contain the same product more than once", ex.getMessage());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipe_withNegativeAmount_throwsException() {
        User user = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(user);

        Product product = Product.builder()
                .id(10L)
                .name("Sugar")
                .ownerUser(user)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        RecipeIngredientDto ingDto = RecipeIngredientDto.builder()
                .productId(10L)
                .amount(-1.0)
                .build();

        RecipeDto dto = RecipeDto.builder()
                .name("Bad amount")
                .ingredients(List.of(ingDto))
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.createRecipe(dto)
        );

        assertEquals("Ingredient amount must be positive", ex.getMessage());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void getRecipeById_existingPublicRecipe_asUser_returnsDto() {
        User user = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(user);

        Recipe entity = Recipe.builder()
                .id(5L)
                .name("Public")
                .ownerUser(null)
                .ingredients(new ArrayList<>())
                .build();

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(entity));

        RecipeDto dto = recipeService.getRecipeById(5L);

        assertEquals(5L, dto.getId());
        assertEquals("Public", dto.getName());
        assertNull(dto.getOwnerUserId());
    }

    @Test
    void getRecipeById_forOtherUser_throwsException() {
        User current = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(current);

        User owner = User.builder()
                .id(2L)
                .username("other")
                .email("other@mealcraft.org")
                .password("pass")
                .build();

        Recipe entity = Recipe.builder()
                .id(5L)
                .name("Private")
                .ownerUser(owner)
                .ingredients(new ArrayList<>())
                .build();

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(entity));

        EntityDoesNotExistException ex = assertThrows(
                EntityDoesNotExistException.class,
                () -> recipeService.getRecipeById(5L)
        );

        assertEquals("Recipe", ex.getEntityType());
        assertEquals("id", ex.getFieldName());
        assertEquals("5", ex.getFieldValue());
    }

    @Test
    void deleteRecipeById_asOwner_deletesRecipe() {
        User current = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(current);

        Recipe recipe = Recipe.builder()
                .id(10L)
                .name("To delete")
                .ownerUser(current)
                .ingredients(new ArrayList<>())
                .build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.findAllByBaseRecipe(recipe)).thenReturn(List.of());

        recipeService.deleteRecipeById(10L);

        verify(recipeRepository, times(1)).deleteById(10L);
    }

    @Test
    void deleteRecipeById_asOtherUser_throwsException() {
        User current = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(current);

        User owner = User.builder()
                .id(2L)
                .username("other")
                .email("other@mealcraft.org")
                .password("pass")
                .build();

        Recipe recipe = Recipe.builder()
                .id(10L)
                .name("Foreign")
                .ownerUser(owner)
                .ingredients(new ArrayList<>())
                .build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));

        assertThrows(EntityDoesNotExistException.class,
                () -> recipeService.deleteRecipeById(10L));

        verify(recipeRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRecipeById_publicRecipe_asAdmin_deletesRecipe() {
        mockAdmin();

        Recipe recipe = Recipe.builder()
                .id(10L)
                .name("Public")
                .ownerUser(null)
                .ingredients(new ArrayList<>())
                .build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.findAllByBaseRecipe(recipe)).thenReturn(List.of());

        recipeService.deleteRecipeById(10L);

        verify(recipeRepository, times(1)).deleteById(10L);
    }

    @Test
    void getAllRecipes_asAdmin_returnsOnlyPublicRecipes() {
        mockAdmin();

        Recipe publicRecipe = Recipe.builder()
                .id(1L)
                .name("Public")
                .ownerUser(null)
                .ingredients(new ArrayList<>())
                .build();

        when(recipeRepository.findAllByOwnerUserIsNull())
                .thenReturn(List.of(publicRecipe));

        List<RecipeDto> result = recipeService.getAllRecipes();

        assertEquals(1, result.size());
        assertEquals("Public", result.get(0).getName());
        verify(recipeRepository, times(1)).findAllByOwnerUserIsNull();
        verify(recipeRepository, never())
                .findAllByOwnerUserIsNullOrOwnerUser_Id(anyLong());
    }

    @Test
    void getAllRecipes_asUser_returnsPublicAndOwnRecipes() {
        User user = User.builder()
                .id(1L)
                .username("vika")
                .email("vika@mealcraft.org")
                .password("pass")
                .build();
        mockCurrentUser(user);

        Recipe publicRecipe = Recipe.builder()
                .id(1L)
                .name("Public")
                .ownerUser(null)
                .ingredients(new ArrayList<>())
                .build();

        Recipe ownRecipe = Recipe.builder()
                .id(2L)
                .name("Own")
                .ownerUser(user)
                .ingredients(new ArrayList<>())
                .build();

        when(recipeRepository.findAllByOwnerUserIsNullOrOwnerUser_Id(1L))
                .thenReturn(List.of(publicRecipe, ownRecipe));

        List<RecipeDto> result = recipeService.getAllRecipes();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("Public")));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals("Own")));

        verify(recipeRepository, times(1))
                .findAllByOwnerUserIsNullOrOwnerUser_Id(1L);
        verify(recipeRepository, never())
                .findAllByOwnerUserIsNull();
    }

}