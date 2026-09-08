//package org.l5g7.mealcraft.unittest;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.l5g7.mealcraft.app.mealplan.internal.MealPlan;
//import org.l5g7.mealcraft.app.mealplan.MealPlanDto;
//import org.l5g7.mealcraft.app.mealplan.internal.MealPlanRepository;
//import org.l5g7.mealcraft.app.mealplan.internal.MealPlanServiceImpl;
//import org.l5g7.mealcraft.app.products.Product;
//import org.l5g7.mealcraft.app.recipeingredient.RecipeIngredient;
//import org.l5g7.mealcraft.app.recipes.Recipe;
//import org.l5g7.mealcraft.app.recipes.RecipeRepository;
//import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItemRepository;
//import org.l5g7.mealcraft.app.units.Unit;
//import org.l5g7.mealcraft.app.user.User;
//import org.l5g7.mealcraft.app.user.UserRepository;
//import org.l5g7.mealcraft.enums.MealPlanColor;
//import org.l5g7.mealcraft.enums.MealStatus;
//import org.l5g7.mealcraft.exception.EntityDoesNotExistException;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.Date;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MealPlanServiceImplTest {
//
//    @Mock
//    private MealPlanRepository mealPlanRepository;
//
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private RecipeRepository recipeRepository;
//    @Mock
//    private ShoppingItemRepository shoppingItemRepository;
//    @Mock
//    private org.l5g7.mealcraft.app.shoppingitem.ShoppingItemService shoppingItemService;
//
//    @InjectMocks
//    private MealPlanServiceImpl mealPlanService;
//
//    private User testUser;
//    private User testUser2;
//    private Recipe testRecipe;
//    private MealPlan mealPlan1;
//    private MealPlan mealPlan2;
//    private MealPlan mealPlan3;
//    private Recipe baseRecipe;
//    private Unit unit1;
//    private Unit unit2;
//    private Product product1;
//    private Product product2;
//    private Recipe recipe1;
//    private Recipe recipe2;
//    private RecipeIngredient recipeIngredient11;
//    private RecipeIngredient recipeIngredient12;
//    private RecipeIngredient recipeIngredient21;
//
//
//    LocalDate localPlanDate1 = LocalDate.of(2025, 11, 3); // 3 Nov 2025
//    LocalDate localPlanDate2 = LocalDate.of(2025, 11, 5);   // 5 Nov 2025
//
//    Date planDate1 = Date.from(localPlanDate1.atStartOfDay(ZoneId.systemDefault()).toInstant());
//    Date planDate2 = Date.from(localPlanDate2.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//
//    @BeforeEach
//    void setUp() {
//
//        testUser = User.builder()
//                .id(1L)
//                .build();
//
//        testUser2 = User.builder()
//                .id(2L)
//                .build();
//
//        testRecipe = Recipe.builder()
//                .id(100L)
//                .name("Test Recipe")
//                .createdAt(new Date())
//                .build();
//
//        mealPlan1 = MealPlan.builder()
//                .id(10L)
//                .userOwner(testUser)
//                .recipe(testRecipe)
//                .planDate(planDate1) // 3 Nov 2025
//                .servings(2)
//                .status(MealStatus.PLANNED)
//                .color(MealPlanColor.BLUE)
//                .build();
//
//        mealPlan2 = MealPlan.builder()
//                .id(11L)
//                .userOwner(testUser)
//                .recipe(testRecipe)
//                .planDate(planDate2) // 5 Nov 2025
//                .servings(3)
//                .status(MealStatus.PLANNED)
//                .color(MealPlanColor.ORANGE)
//                .build();
//
//        mealPlan3 = MealPlan.builder()
//                .id(12L)
//                .userOwner(testUser2)
//                .recipe(testRecipe)
//                .planDate(planDate2) // 5 Nov 2025
//                .servings(10)
//                .status(MealStatus.CANCELLED)
//                .color(MealPlanColor.PURPLE)
//                .build();
//
//        baseRecipe = Recipe.builder()
//                .id(2L)
//                .name("Base Soup")
//                .createdAt(new Date())
//                .ownerUser(testUser)
//                .ingredients(List.of())
//                .build();
//
//        unit2 = Unit.builder()
//                .name("pc")
//                .build();
//
//        product1 = Product.builder()
//                .id(1L)
//                .name("Beetroot")
//                .defaultUnit(unit2)
//                .build();
//
//        unit1 = Unit.builder()
//                .id(1L)
//                .name("liter")
//                .build();
//
//        product2 = Product.builder()
//                .id(2L)
//                .name("Water")
//                .defaultUnit(unit1)
//                .build();
//
//        recipe1 = Recipe.builder()
//                .id(1L)
//                .name("Borshch")
//                .createdAt(new Date())
//                .ownerUser(testUser)
//                .baseRecipe(baseRecipe)
//                .imageUrl("https://example.com/borshch.jpg")
//                .build();
//
//        recipeIngredient11 = RecipeIngredient.builder()
//                .product(product1)
//                .recipe(recipe1)
//                .amount(1d)
//                .build();
//
//        recipeIngredient12 = RecipeIngredient.builder()
//                .product(product2)
//                .recipe(recipe1)
//                .amount(1d)
//                .build();
//
//        recipe1.setIngredients(List.of(recipeIngredient11, recipeIngredient12));
//
//        recipe2 = Recipe.builder()
//                .id(2L)
//                .name("Stewed beetroot")
//                .createdAt(new Date())
//                .ownerUser(null)
//                .baseRecipe(null)
//                .imageUrl(null)
//                .build();
//
//        recipeIngredient21 = RecipeIngredient.builder()
//                .product(product1)
//                .recipe(recipe2)
//                .amount(1d)
//                .build();
//
//        recipe2.setIngredients(List.of(recipeIngredient21));
//
//    }
//
//    @Test
//    void testGetAllMealPlans_ReturnsMealPlans() {
//        when(mealPlanRepository.findAll())
//                .thenReturn(List.of(mealPlan1, mealPlan2, mealPlan3));
//
//        List<MealPlanDto> result = mealPlanService.getAllMealPlans();
//
//        assertEquals(3, result.size());
//        assertEquals(10L, result.get(0).getId());
//        assertEquals(11L, result.get(1).getId());
//        assertEquals(12L, result.get(2).getId());
//        verify(mealPlanRepository).findAll();
//    }
//
//    @Test
//    void testGetMealPlanById_ReturnsMealPlan() {
//        when(mealPlanRepository.findById(10L))
//                .thenReturn(Optional.ofNullable(mealPlan1));
//
//        MealPlanDto result = mealPlanService.getMealPlanById(10L);
//
//        assertEquals(10L, result.getId());
//        assertEquals(planDate1, result.getPlanDate());
//        assertEquals(testUser.getId(), result.getUserOwnerId());
//        assertEquals(100L, result.getRecipeId());
//        assertEquals(2, result.getServings());
//        assertEquals(MealStatus.PLANNED, result.getStatus());
//
//        verify(mealPlanRepository).findById(10L);
//    }
//
//    @Test
//    void testGetMealPlanByNotExistedId_Throws() {
//        Long nonExistentId = 999L;
//        when(mealPlanRepository.findById(nonExistentId)).thenReturn(Optional.empty());
//
//        assertThrows(EntityDoesNotExistException.class,
//                () -> mealPlanService.getMealPlanById(nonExistentId));
//    }
//
//    @Test
//    void testGetUserMealPlans_UserExists_ReturnsMealPlans() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(mealPlanRepository.findAllByUserOwner(testUser))
//                .thenReturn(List.of(mealPlan1, mealPlan2));
//
//        List<MealPlanDto> result = mealPlanService.getUserMealPlans(1L);
//
//        assertEquals(2, result.size());
//        assertEquals(10L, result.get(0).getId());
//        assertEquals(11L, result.get(1).getId());
//        verify(userRepository).findById(1L);
//        verify(mealPlanRepository).findAllByUserOwner(testUser);
//    }
//
//    @Test
//    void testGetUserMealPlansBetweenDates_ReturnsFilteredMealPlans() {
//        LocalDate fromDate = LocalDate.of(2025, 11, 2); // 2 Nov 2025
//        LocalDate toDate = LocalDate.of(2025, 11, 4);   // 4 Nov 2025
//
//        Date from = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date to = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(mealPlanRepository.findAllByUserOwnerAndPlanDateBetween(testUser, from, to))
//                .thenReturn(List.of(mealPlan1));
//
//        List<MealPlanDto> result = mealPlanService.getUserMealPlansBetweenDates(1L, from, to);
//
//        assertEquals(1, result.size());
//        assertEquals(mealPlan1.getId(), result.get(0).getId());
//        verify(userRepository).findById(1L);
//        verify(mealPlanRepository).findAllByUserOwnerAndPlanDateBetween(testUser, from, to);
//    }
//
//    @Test
//    void testGetUserMealPlansBetweenDatesWithStatus_ReturnsFilteredMealPlans() {
//        LocalDate fromDate = LocalDate.of(2025, 11, 2); // 2 Nov 2025
//        LocalDate toDate = LocalDate.of(2025, 11, 6);   // 6 Nov 2025
//
//        Date from = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date to = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(mealPlanRepository.findMealPlanByUserOwnerAndStatusAndPlanDateBetween(testUser, MealStatus.PLANNED, from, to))
//                .thenReturn(List.of(mealPlan1));
//
//        List<MealPlanDto> result = mealPlanService.getUserMealPlansBetweenDatesWithStatus(1L, from, to,  MealStatus.PLANNED);
//
//        assertEquals(1, result.size());
//        assertEquals(mealPlan1.getId(), result.get(0).getId());
//        verify(userRepository).findById(1L);
//        verify(mealPlanRepository).findMealPlanByUserOwnerAndStatusAndPlanDateBetween(testUser, MealStatus.PLANNED, from, to);
//    }
//
//    @Test
//    void testGetUserMealPlansBetweenDatesWithNotStatus_ReturnsFilteredMealPlans() {
//        LocalDate fromDate = LocalDate.of(2025, 11, 4); // 4 Nov 2025
//        LocalDate toDate = LocalDate.of(2025, 11, 7);   // 7 Nov 2025
//
//        Date from = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date to = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(mealPlanRepository.findMealPlanByUserOwnerAndStatusNotAndPlanDateBetween(testUser, MealStatus.CANCELLED,from, to))
//                .thenReturn(List.of(mealPlan1));
//
//        List<MealPlanDto> result = mealPlanService.getUserMealPlansBetweenDatesWithNotStatus(1L,from, to, MealStatus.CANCELLED);
//
//        assertEquals(1, result.size());
//        assertEquals(mealPlan1.getId(), result.get(0).getId());
//        verify(userRepository).findById(1L);
//        verify(mealPlanRepository).findMealPlanByUserOwnerAndStatusNotAndPlanDateBetween(testUser, MealStatus.CANCELLED, from, to);
//    }
//
//    @Test
//    void createMealPlan_shouldSaveMealPlan_whenValidDto() {
//
//        MealPlanDto dto = MealPlanDto.builder()
//                .userOwnerId(1L)
//                .recipeId(2L)
//                .planDate(new Date())
//                .servings(3)
//                .status(MealStatus.PLANNED)
//                .name(recipe2.getName())
//                .color(MealPlanColor.LIGHT_BLUE.getHex())
//                .build();
//
//        User user = new User();
//        user.setId(1L);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe2));
//
//        mealPlanService.createMealPlan(dto);
//
//        verify(mealPlanRepository, times(1)).save(any(MealPlan.class));
//    }
//
//    @Test
//    void createMealPlan_shouldThrowException_whenUserNotFound() {
//        MealPlanDto dto = MealPlanDto.builder()
//                .userOwnerId(1L)
//                .recipeId(2L)
//                .planDate(new Date())
//                .servings(2)
//                .status(MealStatus.PLANNED)
//                .build();
//
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThrows(EntityDoesNotExistException.class,
//                () -> mealPlanService.createMealPlan(dto));
//    }
//
//    @Test
//    void createMealPlan_shouldThrowException_whenRecipeNotFound() {
//
//        MealPlanDto dto = MealPlanDto.builder()
//                .userOwnerId(1L)
//                .recipeId(2L)
//                .planDate(new Date())
//                .servings(2)
//                .status(MealStatus.PLANNED)
//                .build();
//
//        User user = new User();
//        user.setId(1L);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(recipeRepository.findById(2L)).thenReturn(Optional.empty());
//
//        assertThrows(EntityDoesNotExistException.class,
//                () -> mealPlanService.createMealPlan(dto));
//    }
//
//    @Test
//    void updateMealPlan_shouldUpdateExistingMealPlan() {
//
//        Long id = 5L;
//
//        MealPlan existing = MealPlan.builder()
//                .id(id)
//                .userOwner(new User())
//                .recipe(recipe1)
//                .planDate(new Date())
//                .servings(1)
//                .status(MealStatus.PLANNED)
//                .color(MealPlanColor.BLUE)
//                .build();
//
//        User newUser = new User();
//        newUser.setId(1L);
//
//        Recipe newRecipe = Recipe.builder()
//                .id(2L)
//                .name("New Recipe")
//                .createdAt(new Date())
//                .build();
//
//        MealPlanDto dto = MealPlanDto.builder()
//                .userOwnerId(1L)
//                .recipeId(2L)
//                .planDate(new Date())
//                .servings(5)
//                .status(MealStatus.COOKED)
//                .color(MealPlanColor.GREEN.getHex())
//                .build();
//
//        RecipeIngredient recipeIngredientNew = RecipeIngredient.builder()
//                .product(product2)
//                .recipe(newRecipe)
//                .amount(1d)
//                .build();
//
//        newRecipe.setIngredients(List.of(recipeIngredientNew));
//
//        when(mealPlanRepository.findById(id)).thenReturn(Optional.of(existing));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(newUser));
//        when(recipeRepository.findById(2L)).thenReturn(Optional.of(newRecipe));
//
//        mealPlanService.updateMealPlan(id, dto);
//
//        verify(mealPlanRepository).save(existing);
//        assertEquals(newUser, existing.getUserOwner());
//        assertEquals(newRecipe, existing.getRecipe());
//        assertEquals(5, existing.getServings());
//        assertEquals(MealStatus.COOKED, existing.getStatus());
//        assertEquals(MealPlanColor.GREEN, existing.getColor());
//    }
//
//    @Test
//    void updateMealPlan_shouldThrowException_whenMealPlanNotFound() {
//        when(mealPlanRepository.findById(100L)).thenReturn(Optional.empty());
//
//        MealPlanDto dto = MealPlanDto.builder()
//                .userOwnerId(1L)
//                .recipeId(2L)
//                .planDate(new Date())
//                .servings(5)
//                .status(MealStatus.COOKED)
//                .build();
//
//        assertThrows(EntityDoesNotExistException.class,
//                () -> mealPlanService.updateMealPlan(100L, dto));
//    }
//
//    @Test
//    void patchMealPlan_shouldPatchOnlyProvidedFields() {
//
//        Long id = 7L;
//
//        MealPlan existing = MealPlan.builder()
//                .id(id)
//                .userOwner(testUser2)
//                .recipe(testRecipe)
//                .planDate(planDate1)
//                .servings(2)
//                .status(MealStatus.COOKED)
//                .build();
//
//        Recipe newRecipe = Recipe.builder()
//                .id(99L)
//                .name("Test2 Recipe")
//                .createdAt(new Date())
//                .build();
//
//        RecipeIngredient recipeIngredientNew = RecipeIngredient.builder()
//                .product(product2)
//                .recipe(newRecipe)
//                .amount(1d)
//                .build();
//
//        newRecipe.setIngredients(List.of(recipeIngredientNew));
//
//        MealPlanDto dto = MealPlanDto.builder()
//                .recipeId(99L)
//                .build();
//
//        when(mealPlanRepository.findById(id)).thenReturn(Optional.of(existing));
//        when(recipeRepository.findById(99L)).thenReturn(Optional.of(newRecipe));
//
//        mealPlanService.patchMealPlan(id, dto);
//
//        verify(mealPlanRepository).save(existing);
//        assertEquals(newRecipe, existing.getRecipe());
//        assertEquals(testUser2, existing.getUserOwner());
//        assertEquals(planDate1, existing.getPlanDate());
//        assertEquals(2, existing.getServings());
//        assertEquals(MealStatus.COOKED, existing.getStatus());
//    }
//
//    @Test
//    void patchMealPlan_shouldThrowException_whenEntityNotFound() {
//        when(mealPlanRepository.findById(777L)).thenReturn(Optional.empty());
//        MealPlanDto dto = MealPlanDto.builder()
//                .userOwnerId(testUser2.getId())
//                .recipeId(2L)
//                .planDate(new Date())
//                .servings(5)
//                .status(MealStatus.COOKED)
//                .build();
//
//        assertThrows(EntityDoesNotExistException.class,
//                () -> mealPlanService.patchMealPlan(777L, dto));
//    }
//
//    @Test
//    void deleteMealPlan_shouldCallRepositoryDelete() {
//        MealPlan mealPlanToDelete = MealPlan.builder()
//                .id(55L)
//                .userOwner(testUser)
//                .recipe(recipe1) // recipe1 has ingredients set up in @BeforeEach
//                .planDate(new Date())
//                .servings(2)
//                .status(MealStatus.PLANNED)
//                .color(MealPlanColor.BLUE)
//                .build();
//
//        when(mealPlanRepository.findById(55L)).thenReturn(Optional.of(mealPlanToDelete));
//
//        mealPlanService.deleteMealPlan(55L);
//
//        verify(mealPlanRepository).findById(55L);
//        verify(mealPlanRepository).deleteById(55L);
//    }
//
//
//}