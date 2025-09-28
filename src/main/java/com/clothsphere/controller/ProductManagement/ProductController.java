package com.clothsphere.controller.ProductManagement;

import com.clothsphere.model.ProductManagement.Products;
import com.clothsphere.service.ProductManagement.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:63342")
public class ProductController {

    @Autowired
    private ProductsService productsService;

    // ✅ Create or Update product
    @PostMapping("/save")
    public Products saveProduct(@RequestBody Products product) {
        return productsService.saveOrUpdate(product);
    }

    // ✅ Get all products
    @GetMapping
    public List<Products> getAllProducts() {
        return productsService.getAll();
    }

    // ✅ Get product by ID
    @GetMapping("/{id}")
    public Products getProductById(@PathVariable Long id) {
        return productsService.getById(id);
    }

    // ✅ Delete product
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productsService.delete(id);
        return "Product deleted successfully!";
    }
}




