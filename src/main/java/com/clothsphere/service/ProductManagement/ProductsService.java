package com.clothsphere.service.ProductManagement;

import com.clothsphere.model.ProductManagement.Products;
import com.clothsphere.repository.ProductManagement.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    @Autowired //inject dependencies automatically
    private ProductRepository productsRepository;

    // ✅ Save or Update product

    public Products saveOrUpdate(Products product) {
        if (product.getId() != null) {
            // Check if product exists in DB
            Products existing = productsRepository.findById(product.getId()).orElse(null);
            if (existing != null) {
                // Update existing product
                existing.setName(product.getName());
                existing.setCode(product.getCode());
                existing.setCategory(product.getCategory());
                existing.setDescription(product.getDescription());
                existing.setPrice(product.getPrice());
                existing.setStock(product.getStock());
                existing.setImage(product.getImage());
                return productsRepository.save(existing);
            }
        }
        // If new product or no existing found
        return productsRepository.save(product);
    }


    // ✅ Get all products
    public List<Products> getAll() {
        return productsRepository.findAll();
    }

    // ✅ Get product by ID
    public Products getById(Long id) {
        return productsRepository.findById(id).orElse(null);
    }

    // ✅ Delete product
    public void delete(Long id) {
        productsRepository.deleteById(id);
    }
}
