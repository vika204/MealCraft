package org.l5g7.mealcraft.app.shoppingitem.internal;

import org.l5g7.mealcraft.app.products.Product;
import org.l5g7.mealcraft.app.products.ProductRepository;
import org.l5g7.mealcraft.app.shoppingitem.ShoppingItemDto;
import org.l5g7.mealcraft.app.shoppingitem.ShoppingItemService;
import org.l5g7.mealcraft.app.user.User;
import org.l5g7.mealcraft.app.user.UserRepository;
import org.l5g7.mealcraft.exception.EntityDoesNotExistException;
import org.l5g7.mealcraft.mealcraftstarterexternalrecipes.RecipeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingItemServiceImpl implements ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private static final String ENTITY_NAME_PRODUCT = "Product";

    @Autowired
    public ShoppingItemServiceImpl(ShoppingItemRepository shoppingItemRepository, ProductRepository productRepository, UserRepository userRepository, RecipeProvider recipeProvider) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ShoppingItemDto> getAllShoppingItems() {
        List<ShoppingItem> entities = shoppingItemRepository.findAll();

        return entities.stream().map(entity -> ShoppingItemDto.builder()
                    .id(entity.getId())
                    .name(entity.getProduct().getName())
                    .userOwnerId(entity.getUserOwner().getId())
                    .productId(entity.getProduct().getId())
                    .requiredQty(entity.getRequiredQty())
                    .status(entity.getStatus())
                    .unitName(entity.getProduct().getDefaultUnit().getName())
                    .build()).toList();
    }

    @Override
    public List<ShoppingItemDto> getUserShoppingItems(Long userId) {
        List<ShoppingItem> entities = shoppingItemRepository.findByUserOwnerId(userId);

        return entities.stream().map(entity -> ShoppingItemDto.builder()
                    .id(entity.getId())
                    .name(entity.getProduct().getName())
                    .userOwnerId(entity.getUserOwner().getId())
                    .productId(entity.getProduct().getId())
                    .requiredQty(entity.getRequiredQty())
                    .status(entity.getStatus())
                    .unitName(entity.getProduct().getDefaultUnit().getName())
                    .build()).toList();
    }

    @Override
    public ShoppingItemDto getShoppingItemById(Long id) {
        Optional<ShoppingItem> shoppingItem = shoppingItemRepository.findById(id);

        if (shoppingItem.isPresent()) {
            ShoppingItem entity = shoppingItem.get();

            return new ShoppingItemDto(
                    entity.getId(),
                    entity.getProduct().getName(),
                    entity.getUserOwner().getId(),
                    entity.getProduct().getId(),
                    entity.getRequiredQty(),
                    entity.getStatus(),
                    entity.getProduct().getDefaultUnit().getName(),
                    null
            );
        } else {
            throw new EntityDoesNotExistException("ShoppingItem", String.valueOf(id));
        }
    }

    @Override
    public void createShoppingItem(ShoppingItemDto shoppingItemDto) {
        User user = userRepository.findById(shoppingItemDto.getUserOwnerId())
                .orElseThrow(() -> new EntityDoesNotExistException("User", String.valueOf(shoppingItemDto.getUserOwnerId())));
        Product product = productRepository.findById(shoppingItemDto.getProductId())
                .orElseThrow(() -> new EntityDoesNotExistException(ENTITY_NAME_PRODUCT, String.valueOf(shoppingItemDto.getProductId())));

        double roundedQty = Math.round(shoppingItemDto.getRequiredQty() * 100.0) / 100.0;

        ShoppingItem entity = ShoppingItem.builder()
                .id(shoppingItemDto.getId())
                .userOwner(user)
                .product(product)
                .requiredQty(roundedQty)
                .status(shoppingItemDto.getStatus())
                .boughtAt(shoppingItemDto.getBoughtAt())
                .build();


        shoppingItemRepository.save(entity);

    }

    @Override
    public void updateShoppingItem(Long id, ShoppingItemDto shoppingItemDto) {
        Optional<ShoppingItem> existing = shoppingItemRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityDoesNotExistException("ShoppingItem", String.valueOf(id));
        }
        User user = userRepository.findById(shoppingItemDto.getUserOwnerId())
                .orElseThrow(() -> new EntityDoesNotExistException("User", String.valueOf(shoppingItemDto.getUserOwnerId())));
        Product product = productRepository.findById(shoppingItemDto.getProductId())
                .orElseThrow(() -> new EntityDoesNotExistException(ENTITY_NAME_PRODUCT, String.valueOf(shoppingItemDto.getProductId())));

        double roundedQty = Math.round(shoppingItemDto.getRequiredQty() * 100.0) / 100.0;

        existing.ifPresent(shoppingItem -> {
            shoppingItem.setProduct(product);
            shoppingItem.setUserOwner(user);
            shoppingItem.setRequiredQty(roundedQty);
            shoppingItem.setStatus(shoppingItemDto.getStatus());
            shoppingItemRepository.save(shoppingItem);
        });
    }

    @Override
    public void patchShoppingItem(Long id, ShoppingItemDto patch) {
        Optional<ShoppingItem> existing = shoppingItemRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityDoesNotExistException("ShoppingItem", String.valueOf(id));
        }
        existing.ifPresent(shoppingItem -> {
            if (patch.getRequiredQty() != null) {
                double roundedQty = Math.round(patch.getRequiredQty() * 100.0) / 100.0;
                shoppingItem.setRequiredQty(roundedQty);
            }
            if (patch.getStatus() != null) {
                shoppingItem.setStatus(patch.getStatus());
            }
            if (patch.getProductId() != null) {
                shoppingItem.setProduct(productRepository.findById(patch.getProductId())
                        .orElseThrow(() -> new EntityDoesNotExistException(ENTITY_NAME_PRODUCT, String.valueOf(patch.getProductId()))));
            }
            if (patch.getUserOwnerId() != null) {
                shoppingItem.setUserOwner(userRepository.findById(patch.getUserOwnerId())
                        .orElseThrow(() -> new EntityDoesNotExistException("User", String.valueOf(patch.getUserOwnerId()))));
            }
            if (patch.getBoughtAt() != null) {
                shoppingItem.setBoughtAt(patch.getBoughtAt());
            }
            shoppingItemRepository.save(shoppingItem);
        });
    }

    @Override
    public void deleteShoppingItemById(Long id) {
        shoppingItemRepository.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id) {
        Optional<ShoppingItem> existing = shoppingItemRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityDoesNotExistException("ShoppingItem", String.valueOf(id));
        }
        existing.ifPresent(shoppingItem -> {
            shoppingItem.setStatus(!shoppingItem.getStatus());
            shoppingItem.setBoughtAt(new Date(System.currentTimeMillis()));
            shoppingItemRepository.save(shoppingItem);
        });
    }

    @Override
    public void addShoppingItem(ShoppingItemDto shoppingItemDto){
        List<ShoppingItem> userShoppingItems = shoppingItemRepository.findByUserOwnerId(shoppingItemDto.getUserOwnerId());
        Product product = productRepository.findById(shoppingItemDto.getProductId())
                .orElseThrow(() -> new EntityDoesNotExistException(ENTITY_NAME_PRODUCT, String.valueOf(shoppingItemDto.getProductId())));

        Double remember = 0D;
        boolean found = false;
        for(ShoppingItem item: userShoppingItems){
            if(item.getProduct().equals(product) && Boolean.TRUE.equals(item.getStatus())) {
                remember = item.getRequiredQty();
                found = true;
                deleteShoppingItemById(item.getId());
            }
        }
        userShoppingItems = shoppingItemRepository.findByUserOwnerId(shoppingItemDto.getUserOwnerId());

        for(ShoppingItem item: userShoppingItems){
            if(item.getProduct().equals(product)){

                setQty(item, found,shoppingItemDto, remember);

                if(item.getRequiredQty()<0.01){
                    shoppingItemRepository.delete(item);
                    return;
                }
                if(shoppingItemDto.getRequiredQty()>0){
                    item.setStatus(false);
                }
                shoppingItemRepository.save(item);
                return;
            }
        }
        finalActions(found, remember, shoppingItemDto);
    }

    private void finalActions(boolean found, Double remember, ShoppingItemDto shoppingItemDto) {
        if(found){
            double newCount = remember+shoppingItemDto.getRequiredQty();
            if(newCount<=remember){
                return;
            } else {
                shoppingItemDto.setRequiredQty(remember-shoppingItemDto.getRequiredQty());
            }
            createShoppingItem(shoppingItemDto);
            return;
        }
        createShoppingItem(shoppingItemDto);
    }

    private void setQty(ShoppingItem item, boolean found, ShoppingItemDto shoppingItemDto, Double remember) {
        if(found){
            item.setRequiredQty(shoppingItemDto.getRequiredQty() + remember);
        } else {
            item.setRequiredQty(shoppingItemDto.getRequiredQty() + item.getRequiredQty());
        }
    }

    @Override
    public void removeShoppingItem(ShoppingItemDto shoppingItemDto){
        List<ShoppingItem> userShoppingItems = shoppingItemRepository.findByUserOwnerId(shoppingItemDto.getUserOwnerId());
        Product product = productRepository.findById(shoppingItemDto.getProductId())
                .orElseThrow(() -> new EntityDoesNotExistException(ENTITY_NAME_PRODUCT, String.valueOf(shoppingItemDto.getProductId())));

        for(ShoppingItem item: userShoppingItems){
            if(item.getProduct().equals(product)){
                if(item.getRequiredQty() <= shoppingItemDto.getRequiredQty()){
                    shoppingItemRepository.delete(item);
                } else {
                    item.setRequiredQty(item.getRequiredQty() - shoppingItemDto.getRequiredQty());
                    shoppingItemRepository.save(item);
                }
                return;
            }
        }
    }



}