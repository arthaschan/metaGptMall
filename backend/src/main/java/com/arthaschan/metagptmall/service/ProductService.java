package com.arthaschan.metagptmall.service;

import com.arthaschan.metagptmall.config.RedisConfig;
import com.arthaschan.metagptmall.dto.PageResponse;
import com.arthaschan.metagptmall.dto.ProductDto;
import com.arthaschan.metagptmall.entity.Product;
import com.arthaschan.metagptmall.exception.ApiException;
import com.arthaschan.metagptmall.mapper.ProductMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final String CACHE_KEY_PREFIX = "ecom:product:";

    private final ProductMapper productMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisConfig redisConfig;
    private final ObjectMapper objectMapper;

    public ProductService(ProductMapper productMapper,
                          RedisTemplate<String, String> redisTemplate,
                          RedisConfig redisConfig,
                          ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
        this.redisConfig = redisConfig;
        this.objectMapper = objectMapper;
    }

    public PageResponse<ProductDto> listProducts(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        List<ProductDto> items = productMapper.findActiveProducts(offset, size)
                .stream().map(ProductDto::from).collect(Collectors.toList());
        long total = productMapper.countActiveProducts();
        return PageResponse.of(items, page, size, total);
    }

    public ProductDto getProduct(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, ProductDto.class);
            } catch (JsonProcessingException e) {
                // ignore cache read error; fall through to DB
            }
        }
        Product product = productMapper.findById(id);
        if (product == null) {
            throw ApiException.notFound("Product not found: " + id);
        }
        ProductDto dto = ProductDto.from(product);
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(redisConfig.getProductTtlSeconds()));
        } catch (JsonProcessingException e) {
            // best-effort cache fill; ignore
        }
        return dto;
    }
}
