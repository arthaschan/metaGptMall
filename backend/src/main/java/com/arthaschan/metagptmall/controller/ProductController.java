package com.arthaschan.metagptmall.controller;

import com.arthaschan.metagptmall.dto.PageResponse;
import com.arthaschan.metagptmall.dto.ProductDto;
import com.arthaschan.metagptmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List active products (paginated)")
    public ResponseEntity<PageResponse<ProductDto>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.listProducts(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product detail (Redis cached, TTL=60s)")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }
}
