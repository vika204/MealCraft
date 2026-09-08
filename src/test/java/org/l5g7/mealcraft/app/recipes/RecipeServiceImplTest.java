package org.l5g7.mealcraft.app.recipes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
import org.l5g7.mealcraft.app.products.Product;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredient;
import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredientDto;
import org.l5g7.mealcraft.app.units.Unit;
import org.l5g7.mealcraft.app.user.CurrentUserProvider;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.l5g7.mealcraft.exception.EntityDoesNotExistException;
import org.l5g7.mealcraft.mealcraftstarterexternalrecipes.ExternalRecipe;
import org.l5g7.mealcraft.mealcraftstarterexternalrecipes.RecipeProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeProvider recipeProvider;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    @Mock
    private MealPlanRepository mealPlanRepository;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Recipe testRecipe;
    private RecipeDto testRecipeDto;

    @BeforeEach
    void setUp() {
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setName("kg");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testProduct1 = new Product();
        testProduct1.setId(10L);
        testProduct1.setName("Flour");
        testProduct1.setDefaultUnit(unit);

        testProduct2 = new Product();
        testProduct2.setId(20L);
        testProduct2.setName("Sugar");
        testProduct2.setDefaultUnit(unit);

        testRecipe = Recipe.builder()
                .id(100L)
                .name("Test Recipe")
                .ownerUser(null)
                .createdAt(new Date())
                .imageUrl("http://example.com/image.jpg")
                .build();

        RecipeIngredient ingredient1 = RecipeIngredient.builder()
                .id(1L)
                .recipe(testRecipe)
                .product(testProduct1)
                .amount(2.0)
                .build();

        RecipeIngredient ingredient2 = RecipeIngredient.builder()
                .id(2L)
                .recipe(testRecipe)
                .product(testProduct2)
                .amount(1.5)
                .build();

        testRecipe.setIngredients(new java.util.ArrayList<>(Arrays.asList(ingredient1, ingredient2)));

        testRecipeDto = RecipeDto.builder()
                .name("New Recipe")
                .imageUrl("http://example.com/new.jpg")
                .ingredients(Arrays.asList(
                        RecipeIngredientDto.builder().productId(10L).amount(2.0).build(),
                        RecipeIngredientDto.builder().productId(20L).amount(1.5).build()
                ))
                .build();
    }

    @Test
    void getAllRecipes_asAdmin_returnsOnlyPublicRecipes() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findAllByOwnerUserIsNull()).thenReturn(List.of(testRecipe));

        List<RecipeDto> result = recipeService.getAllRecipes();

        assertEquals(1, result.size());
        assertEquals("Test Recipe", result.get(0).getName());
        verify(recipeRepository, times(1)).findAllByOwnerUserIsNull();
        verify(recipeRepository, never()).findAllByOwnerUserIsNullOrOwnerUser_Id(anyLong());
    }

    @Test
    void getAllRecipes_asUser_returnsPublicAndUserRecipes() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(testUser);
        when(recipeRepository.findAllByOwnerUserIsNullOrOwnerUser_Id(1L)).thenReturn(List.of(testRecipe));

        List<RecipeDto> result = recipeService.getAllRecipes();

        assertEquals(1, result.size());
        verify(recipeRepository, times(1)).findAllByOwnerUserIsNullOrOwnerUser_Id(1L);
    }

    @Test
    void getRecipeById_asAdmin_returnsPublicRecipe() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(testRecipe));

        RecipeDto result = recipeService.getRecipeById(100L);

        assertEquals("Test Recipe", result.getName());
        assertEquals(2, result.getIngredients().size());
    }

    @Test
    void getRecipeById_asUser_returnsOwnRecipe() {
        Recipe userRecipe = Recipe.builder()
                .id(101L)
                .name("User Recipe")
                .ownerUser(testUser)
                .createdAt(new Date())
                .ingredients(List.of())
                .build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(testUser);
        when(recipeRepository.findById(101L)).thenReturn(Optional.of(userRecipe));

        RecipeDto result = recipeService.getRecipeById(101L);

        assertEquals("User Recipe", result.getName());
        assertEquals(1L, result.getOwnerUserId());
    }

    @Test
    void getRecipeById_asUser_throwsWhenAccessingOtherUserRecipe() {
        User otherUser = new User();
        otherUser.setId(2L);

        Recipe otherUserRecipe = Recipe.builder()
                .id(102L)
                .name("Other Recipe")
                .ownerUser(otherUser)
                .createdAt(new Date())
                .build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(testUser);
        when(recipeRepository.findById(102L)).thenReturn(Optional.of(otherUserRecipe));

        assertThrows(EntityDoesNotExistException.class, () -> recipeService.getRecipeById(102L));
    }

    @Test
    void getRecipeById_throwsWhenNotFound() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityDoesNotExistException.class, () -> recipeService.getRecipeById(999L));
    }

    @Test
    void createRecipe_asAdmin_savesPublicRecipe() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(20L)).thenReturn(Optional.of(testProduct2));

        recipeService.createRecipe(testRecipeDto);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(1)).save(captor.capture());
        Recipe saved = captor.getValue();
        assertEquals("New Recipe", saved.getName());
        assertNull(saved.getOwnerUser());
        assertEquals(2, saved.getIngredients().size());
    }

    @Test
    void createRecipe_asUser_savesUserRecipe() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(testUser);
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(20L)).thenReturn(Optional.of(testProduct2));

        recipeService.createRecipe(testRecipeDto);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(1)).save(captor.capture());
        Recipe saved = captor.getValue();
        assertEquals(testUser, saved.getOwnerUser());
    }

    @Test
    void createRecipe_withBaseRecipe_linksToBase() {
        Recipe baseRecipe = Recipe.builder().id(50L).name("Base").build();
        testRecipeDto.setBaseRecipeId(50L);

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(50L)).thenReturn(Optional.of(baseRecipe));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(20L)).thenReturn(Optional.of(testProduct2));

        recipeService.createRecipe(testRecipeDto);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(1)).save(captor.capture());
        Recipe saved = captor.getValue();
        assertEquals(baseRecipe, saved.getBaseRecipe());
    }

    @Test
    void createRecipe_throwsWhenNoIngredients() {
        testRecipeDto.setIngredients(List.of());

        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(testRecipeDto));
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipe_throwsWhenNullIngredients() {
        testRecipeDto.setIngredients(null);

        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(testRecipeDto));
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipe_throwsWhenIngredientMissingProduct() {
        testRecipeDto.setIngredients(List.of(
                RecipeIngredientDto.builder().productId(null).amount(2.0).build()
        ));

        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(testRecipeDto));
    }

    @Test
    void createRecipe_throwsWhenDuplicateProduct() {
        testRecipeDto.setIngredients(Arrays.asList(
                RecipeIngredientDto.builder().productId(10L).amount(2.0).build(),
                RecipeIngredientDto.builder().productId(10L).amount(1.0).build()
        ));

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct1));

        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(testRecipeDto));
    }

    @Test
    void createRecipe_throwsWhenNegativeAmount() {
        testRecipeDto.setIngredients(List.of(
                RecipeIngredientDto.builder().productId(10L).amount(-1.0).build()
        ));

        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(testRecipeDto));
    }

    @Test
    void createRecipe_throwsWhenProductNotFound() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityDoesNotExistException.class, () -> recipeService.createRecipe(testRecipeDto));
    }

    @Test
    void updateRecipe_asAdmin_updatesPublicRecipe() {
        Recipe recipeToUpdate = Recipe.builder()
                .id(100L)
                .name("Test Recipe")
                .ownerUser(null)
                .createdAt(new Date())
                .imageUrl("http://example.com/image.jpg")
                .ingredients(new java.util.ArrayList<>())
                .build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(recipeToUpdate));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(20L)).thenReturn(Optional.of(testProduct2));
        testRecipeDto.setName("Updated Recipe");
        recipeService.updateRecipe(100L, testRecipeDto);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(1)).save(captor.capture());
        Recipe saved = captor.getValue();
        assertEquals("Updated Recipe", saved.getName());
    }

    @Test
    void updateRecipe_throwsWhenNotFound() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityDoesNotExistException.class, () -> recipeService.updateRecipe(999L, testRecipeDto));
    }

    @Test
    void updateRecipe_throwsWhenNoIngredients() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(testRecipe));
        testRecipeDto.setIngredients(List.of());

        assertThrows(IllegalArgumentException.class, () -> recipeService.updateRecipe(100L, testRecipeDto));
    }

    @Test
    void patchRecipe_asAdmin_updatesNameOnly() {
        RecipeDto patch = RecipeDto.builder().name("Patched Name").build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(testRecipe));

        recipeService.patchRecipe(100L, patch);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(1)).save(captor.capture());
        Recipe saved = captor.getValue();
        assertEquals("Patched Name", saved.getName());
    }

    @Test
    void patchRecipe_updatesIngredients() {
        Recipe recipeToPatch = Recipe.builder()
                .id(100L)
                .name("Test Recipe")
                .ownerUser(null)
                .createdAt(new Date())
                .imageUrl("http://example.com/image.jpg")
                .ingredients(new java.util.ArrayList<>())
                .build();

        RecipeDto patch = RecipeDto.builder()
                .ingredients(List.of(
                        RecipeIngredientDto.builder().productId(10L).amount(5.0).build()
                ))
                .build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(recipeToPatch));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct1));

        recipeService.patchRecipe(100L, patch);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(1)).save(captor.capture());
        Recipe saved = captor.getValue();
        assertEquals(1, saved.getIngredients().size());
        assertEquals(5.0, saved.getIngredients().get(0).getAmount());
    }

    @Test
    void patchRecipe_throwsWhenEmptyIngredients() {
        RecipeDto patch = RecipeDto.builder().ingredients(List.of()).build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(testRecipe));

        assertThrows(IllegalArgumentException.class, () -> recipeService.patchRecipe(100L, patch));
    }

    @Test
    void deleteRecipeById_asAdmin_deletesPublicRecipe() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.findAllByBaseRecipe(testRecipe)).thenReturn(List.of());
        when(mealPlanRepository.existsByRecipe(testRecipe)).thenReturn(false);
        when(mealPlanRepository.existsByRecipe(testRecipe)).thenReturn(false);
        recipeService.deleteRecipeById(100L);

        verify(recipeRepository).deleteById(100L);
    }

    @Test
    void deleteRecipeById_asUser_deletesOwnRecipe() {
        Recipe userRecipe = Recipe.builder()
                .id(101L)
                .name("User Recipe")
                .ownerUser(testUser)
                .build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(testUser);
        when(recipeRepository.findById(101L)).thenReturn(Optional.of(userRecipe));
        when(recipeRepository.findAllByBaseRecipe(userRecipe)).thenReturn(List.of());
        when(mealPlanRepository.existsByRecipe(userRecipe)).thenReturn(false);

        recipeService.deleteRecipeById(101L);

        verify(recipeRepository).deleteById(101L);
    }

    @Test
    void deleteRecipeById_withChildren_unlinksChildren() {
        Recipe child1 = Recipe.builder().id(200L).baseRecipe(testRecipe).build();
        Recipe child2 = Recipe.builder().id(201L).baseRecipe(testRecipe).build();

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.findAllByBaseRecipe(testRecipe)).thenReturn(Arrays.asList(child1, child2));
        when(mealPlanRepository.existsByRecipe(testRecipe)).thenReturn(false);

        recipeService.deleteRecipeById(100L);

        assertNull(child1.getBaseRecipe());
        assertNull(child2.getBaseRecipe());
        verify(recipeRepository, times(1)).saveAll(anyList());
        verify(recipeRepository, times(1)).deleteById(100L);
    }


    @Test
    void deleteRecipeById_throwsWhenNotFound() {
        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityDoesNotExistException.class, () -> recipeService.deleteRecipeById(999L));
    }

    @Test
    void getRandomRecipe_returnsExternalRecipe() {
        Date now = new Date();
        ExternalRecipe external = new ExternalRecipe(99L, "Random Recipe", "http://example.com/random.jpg", now.toString(), List.of("Ingredient1", "Ingredient2"), List.of("Measure1", "Measure2"));
        when(recipeProvider.getRandomRecipe()).thenReturn(external);

        RecipeDto result = recipeService.getRandomRecipe();

        assertEquals(99L, result.getId());
        assertEquals("Random Recipe", result.getName());
        assertEquals("http://example.com/random.jpg", result.getImageUrl());
    }

    @Test
    void getRecipesByProducts_returnsMatchingRecipes() {
        testProduct1.setOwnerUser(null);
        testProduct2.setOwnerUser(null);

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findAllByOwnerUserIsNull()).thenReturn(List.of(testRecipe));

        List<RecipeDto> result = recipeService.getRecipesByProducts(Arrays.asList("Flour", "Sugar"));

        assertEquals(1, result.size());
        assertEquals("Test Recipe", result.get(0).getName());
    }

    @Test
    void getRecipesByProducts_returnsEmptyWhenNoProducts() {
        List<RecipeDto> result = recipeService.getRecipesByProducts(List.of());

        assertTrue(result.isEmpty());
        verify(recipeRepository, never()).findAllByOwnerUserIsNull();
    }

    @Test
    void getRecipesByProducts_returnsEmptyWhenNullProducts() {
        List<RecipeDto> result = recipeService.getRecipesByProducts(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getRecipesByProducts_filtersNonMatchingRecipes() {
        testProduct1.setOwnerUser(null);
        testProduct2.setOwnerUser(null);

        when(currentUserProvider.getCurrentUserOrNullIfAdmin()).thenReturn(null);
        when(recipeRepository.findAllByOwnerUserIsNull()).thenReturn(List.of(testRecipe));

        List<RecipeDto> result = recipeService.getRecipesByProducts(List.of("Flour"));

        assertTrue(result.isEmpty());
    }
}

