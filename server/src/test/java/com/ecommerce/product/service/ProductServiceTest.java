package com.ecommerce.product.service;

import com.ecommerce.entity.Product;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        Product product1 = buildProduct(1L, "Product 1", 9999);
        Product product2 = buildProduct(2L, "Product 2", 14999);

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        List<ProductResponse> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Product 1");
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_ExistingId_ShouldReturnProduct() {
        Product product = buildProduct(1L, "Test Product", 9999);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Product");
        assertThat(result.getPriceCents()).isEqualTo(9999);

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_NonExistingId_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.getProductById(999L));

        assertThat(exception.getMessage()).contains("Product not found");
        verify(productRepository, times(1)).findById(999L);
    }

    private Product buildProduct(Long id, String title, Integer priceCents) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setDescription(title + " description");
        product.setPriceCents(priceCents);
        product.setCurrency("USD");
        product.setStock(10);
        product.setImageUrl("image.jpg");
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}
