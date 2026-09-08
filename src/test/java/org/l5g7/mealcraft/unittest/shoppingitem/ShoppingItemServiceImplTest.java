package org.l5g7.mealcraft.unittest.shoppingitem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.l5g7.mealcraft.app.products.Product;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItem;
import org.l5g7.mealcraft.app.shoppingitem.ShoppingItemDto;
import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItemRepository;
import org.l5g7.mealcraft.app.shoppingitem.internal.ShoppingItemServiceImpl;
import org.l5g7.mealcraft.app.units.Unit;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.l5g7.mealcraft.exception.EntityDoesNotExistException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingItemServiceImplTest {

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShoppingItemServiceImpl service;

    private User user;
    private Product product;
    private Unit unit;

    @BeforeEach
    void setup() {
        unit = new Unit();
        unit.setId(7L);
        unit.setName("kg");

        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(2L);
        product.setName("Milk");
        product.setDefaultUnit(unit);
    }

    @Test
    void getAllShoppingItems_returnsMappedDtos() {
        ShoppingItem si = ShoppingItem.builder()
                .id(100L)
                .userOwner(user)
                .product(product)
                .requiredQty(2.5)
                .status(false)
                .build();
        when(shoppingItemRepository.findAll()).thenReturn(List.of(si));

        List<ShoppingItemDto> result = service.getAllShoppingItems();
        assertEquals(1, result.size());
        ShoppingItemDto dto = result.get(0);
        assertEquals(100L, dto.getId());
        assertEquals("Milk", dto.getName());
        assertEquals(1L, dto.getUserOwnerId());
        assertEquals(2L, dto.getProductId());
        assertEquals(2.5, dto.getRequiredQty());
        assertFalse(dto.getStatus());
        assertEquals("kg", dto.getUnitName());
    }

    @Test
    void getUserShoppingItems_filtersByUser_andMapsDtos() {
        ShoppingItem si = ShoppingItem.builder()
                .id(101L)
                .userOwner(user)
                .product(product)
                .requiredQty(1.0)
                .status(true)
                .build();
        when(shoppingItemRepository.findByUserOwnerId(1L)).thenReturn(List.of(si));

        List<ShoppingItemDto> result = service.getUserShoppingItems(1L);
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(true, result.get(0).getStatus());
    }

    @Test
    void getShoppingItemById_returnsMappedDto_whenFound() {
        ShoppingItem si = ShoppingItem.builder()
                .id(102L)
                .userOwner(user)
                .product(product)
                .requiredQty(3.0)
                .status(false)
                .build();
        when(shoppingItemRepository.findById(102L)).thenReturn(Optional.of(si));

        ShoppingItemDto dto = service.getShoppingItemById(102L);
        assertEquals(102L, dto.getId());
        assertEquals("Milk", dto.getName());
        assertEquals(1L, dto.getUserOwnerId());
        assertEquals(2L, dto.getProductId());
        assertEquals("kg", dto.getUnitName());
        assertNull(dto.getBoughtAt());
    }

    @Test
    void getShoppingItemById_throwsWhenNotFound() {
        when(shoppingItemRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityDoesNotExistException.class, () -> service.getShoppingItemById(999L));
    }

    @Test
    void createShoppingItem_savesEntity() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .id(100L)
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(2.0)
                .status(false)
                .build();

        service.createShoppingItem(dto);

        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository, times(1)).save(captor.capture());
        ShoppingItem saved = captor.getValue();
        assertEquals(100L, saved.getId());
        assertEquals(user, saved.getUserOwner());
        assertEquals(product, saved.getProduct());
        assertEquals(2.0, saved.getRequiredQty());
        assertEquals(false, saved.getStatus());
    }

    @Test
    void createShoppingItem_throwsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ShoppingItemDto dto = ShoppingItemDto.builder().userOwnerId(1L).productId(2L).build();
        assertThrows(EntityDoesNotExistException.class, () -> service.createShoppingItem(dto));
        verify(shoppingItemRepository, never()).save(any());
    }

    @Test
    void updateShoppingItem_updatesAndSaves_whenFound() {
        ShoppingItem existing = ShoppingItem.builder()
                .id(200L)
                .userOwner(user)
                .product(product)
                .requiredQty(1.0)
                .status(false)
                .build();
        when(shoppingItemRepository.findById(200L)).thenReturn(Optional.of(existing));

        User newUser = new User();
        newUser.setId(5L);
        Product newProduct = new Product();
        newProduct.setId(6L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(newUser));
        when(productRepository.findById(6L)).thenReturn(Optional.of(newProduct));

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(5L)
                .productId(6L)
                .requiredQty(9.0)
                .status(true)
                .build();

        service.updateShoppingItem(200L, dto);

        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository, times(1)).save(captor.capture());
        ShoppingItem saved = captor.getValue();
        assertEquals(newUser, saved.getUserOwner());
        assertEquals(newProduct, saved.getProduct());
        assertEquals(9.0, saved.getRequiredQty());
        assertTrue(saved.getStatus());
    }

    @Test
    void updateShoppingItem_throwsWhenItemNotFound() {
        when(shoppingItemRepository.findById(100L)).thenReturn(Optional.empty());
        ShoppingItemDto dto = ShoppingItemDto.builder().userOwnerId(1L).productId(2L).build();
        assertThrows(EntityDoesNotExistException.class, () -> service.updateShoppingItem(100L, dto));
    }

    @Test
    void patchShoppingItem_updatesSelectedFields_andSaves() {
        ShoppingItem existing = ShoppingItem.builder()
                .id(300L)
                .userOwner(user)
                .product(product)
                .requiredQty(1.0)
                .status(false)
                .build();
        when(shoppingItemRepository.findById(300L)).thenReturn(Optional.of(existing));

        User newUser = new User();
        newUser.setId(8L);
        Product newProduct = new Product();
        newProduct.setId(9L);
        when(userRepository.findById(8L)).thenReturn(Optional.of(newUser));
        when(productRepository.findById(9L)).thenReturn(Optional.of(newProduct));

        Date boughtAt = new Date();
        ShoppingItemDto patch = ShoppingItemDto.builder()
                .requiredQty(5.0)
                .status(true)
                .userOwnerId(8L)
                .productId(9L)
                .boughtAt(boughtAt)
                .build();

        service.patchShoppingItem(300L, patch);

        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository, times(1)).save(captor.capture());
        ShoppingItem saved = captor.getValue();
        assertEquals(5.0, saved.getRequiredQty());
        assertTrue(saved.getStatus());
        assertEquals(newUser, saved.getUserOwner());
        assertEquals(newProduct, saved.getProduct());
        assertEquals(boughtAt, saved.getBoughtAt());
    }

    @Test
    void patchShoppingItem_throwsWhenItemNotFound() {
        when(shoppingItemRepository.findById(404L)).thenReturn(Optional.empty());
        ShoppingItemDto dto = ShoppingItemDto.builder().build();

        assertThrows(EntityDoesNotExistException.class, () -> service.patchShoppingItem(404L, dto));
    }

    @Test
    void deleteShoppingItemById_callsRepositoryDelete() {
        service.deleteShoppingItemById(777L);
        verify(shoppingItemRepository, times(1)).deleteById(777L);
    }

    @Test
    void toggleStatus_flipsBooleanAndSetsBoughtAt() {
        ShoppingItem existing = ShoppingItem.builder()
                .id(100L)
                .userOwner(user)
                .product(product)
                .requiredQty(1.0)
                .status(false)
                .build();
        when(shoppingItemRepository.findById(100L)).thenReturn(Optional.of(existing));

        service.toggleStatus(100L);

        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository, times(1)).save(captor.capture());
        ShoppingItem saved = captor.getValue();
        assertTrue(saved.getStatus());
        assertNotNull(saved.getBoughtAt());
    }

    @Test
    void toggleStatus_throwsWhenNotFound() {
        when(shoppingItemRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityDoesNotExistException.class, () -> service.toggleStatus(999L));
    }

    @Test
    void addShoppingItem_createsNew_whenNoExistingItems() {
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(3.0)
                .build();

        when(shoppingItemRepository.findByUserOwnerId(1L))
                .thenReturn(List.of());
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));


        service.addShoppingItem(dto);

        verify(shoppingItemRepository, times(1)).save(any(ShoppingItem.class));
    }

    @Test
    void addShoppingItem_accumulatesQty_whenItemExistsAndStatusFalse() {
        ShoppingItem existing = ShoppingItem.builder()
                .id(10L)
                .userOwner(user)
                .product(product)
                .requiredQty(2.0)
                .status(false)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(5.0)
                .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(shoppingItemRepository.findByUserOwnerId(1L))
                .thenReturn(List.of(existing));

        service.addShoppingItem(dto);

        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository).save(captor.capture());
        assertEquals(7.0, captor.getValue().getRequiredQty());
    }

    @Test
    void addShoppingItem_findsCompletedItem_statusTrue_andReplacesQty() {
        ShoppingItem completed = ShoppingItem.builder()
                .id(20L)
                .userOwner(user)
                .product(product)
                .requiredQty(4.0)
                .status(true)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(6.0)
                .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(shoppingItemRepository.findByUserOwnerId(1L))
                .thenReturn(List.of(completed))
                .thenReturn(List.of());

        service.addShoppingItem(dto);

        verify(shoppingItemRepository, atLeastOnce()).save(any());
    }

    @Test
    void addShoppingItem_deletesItem_whenQtyGoesBelowThreshold() {
        ShoppingItem existing = ShoppingItem.builder()
                .id(30L)
                .userOwner(user)
                .product(product)
                .requiredQty(0.02)
                .status(false)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(-0.03) // subtraction makes result negative
                .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        when(shoppingItemRepository.findByUserOwnerId(1L))
                .thenReturn(List.of(existing));

        service.addShoppingItem(dto);

        verify(shoppingItemRepository).delete(existing);
    }


    @Test
    void removeShoppingItem_deletesWholeItem_whenQtyBecomesZeroOrLess() {
        ShoppingItem existing = ShoppingItem.builder()
                .id(40L)
                .userOwner(user)
                .product(product)
                .requiredQty(2.0)
                .status(false)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(2.0)
                .build();

        when(shoppingItemRepository.findByUserOwnerId(1L))
                .thenReturn(List.of(existing));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        service.removeShoppingItem(dto);

        verify(shoppingItemRepository).delete(existing);
    }

    @Test
    void removeShoppingItem_reducesQty_whenEnoughLeft() {
        ShoppingItem existing = ShoppingItem.builder()
                .id(41L)
                .userOwner(user)
                .product(product)
                .requiredQty(10.0)
                .status(false)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(3.0)
                .build();

        when(shoppingItemRepository.findByUserOwnerId(1L))
                .thenReturn(List.of(existing));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        service.removeShoppingItem(dto);

        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository).save(captor.capture());
        assertEquals(7.0, captor.getValue().getRequiredQty());
    }

    @Test
    void removeShoppingItem_doesNothing_whenNoMatchingProduct() {
        ShoppingItem other = ShoppingItem.builder()
                .id(50L)
                .userOwner(user)
                .product(new Product()) // different product
                .requiredQty(5.0)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .userOwnerId(1L)
                .productId(2L)
                .requiredQty(1.0)
                .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(shoppingItemRepository.findByUserOwnerId(1L))
                .thenReturn(List.of(other));

        service.removeShoppingItem(dto);

        verify(shoppingItemRepository, never()).save(any());
        verify(shoppingItemRepository, never()).delete(any());
    }

}
