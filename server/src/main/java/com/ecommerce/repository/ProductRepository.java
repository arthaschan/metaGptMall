package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductRepository {

	@Select("""
			SELECT id, title, description, price_cents, currency, stock, image_url, active, created_at, updated_at
			FROM products
			WHERE active = TRUE
			ORDER BY id
			""")
	List<Product> findAll();

	@Select("""
			SELECT id, title, description, price_cents, currency, stock, image_url, active, created_at, updated_at
			FROM products
			WHERE id = #{id} AND active = TRUE
			""")
	Product selectById(Long id);

	default Optional<Product> findById(Long id) {
		return Optional.ofNullable(selectById(id));
	}
}
