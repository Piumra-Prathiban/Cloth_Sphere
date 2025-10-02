package com.clothsphere.controller.ProductManagement;

import com.clothsphere.model.ProductManagement.Products;
import com.clothsphere.service.ProductManagement.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:63342")

public class ProductController {

    @Autowired
    private ProductsService productsService;

    // ✅ Create or Update product with single image
    @PostMapping("/save")
    public Products saveProduct(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("name") String name,
            @RequestParam("code") String code,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        Products product = new Products();
        product.setId(id);
        product.setName(name);
        product.setCode(code);
        product.setCategory(category);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);

        // Handle image file
        if (imageFile != null && !imageFile.isEmpty()) {

            String uploadDir = "static/UploadProduct/";
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            try {
                Path filePath = Paths.get(uploadDir + fileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(fileName); // save filename/path to DB
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return productsService.saveOrUpdate(product);
    }

    @GetMapping("/list")
    public List<Products> getAllProducts() {
        List<Products> products = productsService.getAll();

        products.forEach(p -> {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                p.setImage("/UploadProduct/" + p.getImage());
            }
        });
        return products;
    }


    @GetMapping("/{id}")
    public Products getProductById(@PathVariable Long id) {
        return productsService.getById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productsService.delete(id);
        return "Product deleted successfully!";
    }
}


