package org.example.service;

import org.example.exception.InsufficientStockException;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.ProductType;
import org.example.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductManagerTest {

    @Mock
    private ProductRepository repository;

    private ProductManager manager;

    @BeforeEach
    void setUp() {
        when(repository.load()).thenReturn(List.of());
        manager = new ProductManager(repository);
    }

    @Test
    void addProduct_persistsProductAndRejectsDuplicateId() {
        Product product = Product.builder()
                .type(ProductType.ELECTRONICS)
                .name("Kettle")
                .basePrice(BigDecimal.valueOf(50))
                .availableQuantity(10)
                .build();

        manager.addProduct(product);

        assertThat(manager.getProducts()).containsExactly(product);
        verify(repository).save(List.of(product));

        assertThatThrownBy(() -> manager.addProduct(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(product.getId());
    }

    @Test
    void reserveStockForOrder_throwsAndLeavesStockUntouched_whenNotEnoughQuantity() {
        Product product = Product.builder()
                .type(ProductType.COMPUTER)
                .name("Laptop")
                .basePrice(BigDecimal.valueOf(3000))
                .availableQuantity(2)
                .build();
        manager.addProduct(product);
        clearInvocations(repository);

        OrderItem item = new OrderItem(product, List.of(), 5);

        assertThatThrownBy(() -> manager.reserveStockForOrder(List.of(item)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Laptop");

        assertThat(product.getAvailableQuantity()).isEqualTo(2);
        verify(repository, never()).save(any());
    }
}
