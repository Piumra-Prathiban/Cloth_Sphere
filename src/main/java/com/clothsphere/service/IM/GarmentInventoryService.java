package com.clothsphere.service.IM;

import com.clothsphere.dto.IM.GarmentCreationRequest;
import com.clothsphere.model.IM.Garment;
import com.clothsphere.model.PM.Product;
import com.clothsphere.repository.IM.GarmentRepository;
import com.clothsphere.service.PM.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class GarmentInventoryService {

    @Autowired
    private GarmentService garmentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private GarmentRepository garmentRepository;

    /**
     * Create new garment in both garment table and product table
     */
    @Transactional
    public Garment createNewGarmentWithProduct(GarmentCreationRequest request) {
        // Validate minimum stock requirement
        if (request.getInitialStock() < 10) {
            throw new IllegalArgumentException("Minimum initial stock is 10 pieces");
        }

        // Check for duplicate garment
        if (garmentService.isDuplicateGarment(request.getGarmentType(), request.getSize(), request.getFabricId())) {
            throw new IllegalArgumentException("Garment with this type, size, and fabric already exists");
        }

        // Create garment in garment table
        Garment garment = createGarment(request);

        // Create corresponding product in product table
        createProductForGarment(garment, request);

        // Create initial stock movement
        createInitialStockMovement(garment, request.getInitialStock());

        return garment;
    }

    private Garment createGarment(GarmentCreationRequest request) {
        // Generate garment ID
        String lastGarmentId = getLastGarmentId();
        String newGarmentId = generateGarmentId("GAR", lastGarmentId);

        Garment garment = new Garment();
        garment.setGarmentId(newGarmentId);
        garment.setType(request.getGarmentType());
        garment.setSize(request.getSize());
        garment.setFabricId(request.getFabricId());
        garment.setCreatedAt(LocalDateTime.now());
        garment.setUpdatedAt(LocalDateTime.now());

        garmentRepository.insertGarment(garment);
        return garment;
    }

    private void createProductForGarment(Garment garment, GarmentCreationRequest request) {
        Product product = new Product();

        // Generate product ID
        String productId = productService.generateNextProductId();

        // Set product details
        product.setProductId(productId);
        product.setName(request.getName());
        product.setCode(generateProductCode(garment.getGarmentId()));
        product.setPrice(request.getPrice());
        product.setStock(request.getInitialStock());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setGarmentType(garment.getType());
        product.setSize(garment.getSize());
        product.setFabricId(garment.getFabricId());

        productService.createProduct(product);
    }

    private void createInitialStockMovement(Garment garment, Integer initialStock) {
        // This will create an initial "In" movement with the approved quantity equal to initial stock
        garmentService.createInitialStockMovement(garment.getGarmentId(), initialStock);
    }

    // ADD THIS MISSING METHOD
    public String getLastGarmentId() {
        return garmentRepository.findLastGarmentId();
    }

    private String generateGarmentId(String prefix, String lastId) {
        int number = 1;
        if (lastId != null && !lastId.isEmpty()) {
            number = Integer.parseInt(lastId.substring(prefix.length())) + 1;
        }
        return String.format("%s%03d", prefix, number);
    }

    private String generateProductCode(String garmentId) {
        return "PROD_" + garmentId + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ADD THIS METHOD TO GET NEXT GARMENT ID FOR THE CONTROLLER
    public String getNextGarmentId() {
        String lastGarmentId = getLastGarmentId();
        return generateGarmentId("GAR", lastGarmentId);
    }
}