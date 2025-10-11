package com.clothsphere.service.SOM;

import com.clothsphere.model.SOM.Wishlist;
import com.clothsphere.model.CPM.Product;
import com.clothsphere.repository.SOM.WishlistRepository;
import com.clothsphere.repository.CPM.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Add a product to the wishlist
     */
    public boolean addToWishlist(String buyerId, Long productId) {
        // Check if already in wishlist
        if (wishlistRepository.existsByBuyerIdAndProductId(buyerId, productId)) {
            return false; // Already in wishlist
        }

        // Check if product exists
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setBuyerId(buyerId);
        wishlist.setProductId(productId);
        wishlist.setAddedAt(LocalDateTime.now());

        wishlistRepository.save(wishlist);
        return true;
    }

    /**
     * Remove a product from the wishlist
     */
    public void removeFromWishlist(String buyerId, Long productId) {
        wishlistRepository.deleteByBuyerIdAndProductId(buyerId, productId);
    }

    /**
     * Check if a product is in the wishlist
     */
    public boolean isInWishlist(String buyerId, Long productId) {
        return wishlistRepository.existsByBuyerIdAndProductId(buyerId, productId);
    }

    /**
     * Get all wishlist items for a buyer with product details
     */
    public List<Wishlist> getWishlistWithProducts(String buyerId) {
        List<Wishlist> wishlistItems = wishlistRepository.findByBuyerIdOrderByAddedAtDesc(buyerId);

        // Load product details for each wishlist item
        for (Wishlist item : wishlistItems) {
            productRepository.findById(item.getProductId()).ifPresent(item::setProduct);
        }

        return wishlistItems;
    }

    /**
     * Get wishlist item count for a buyer
     */
    public int getWishlistItemCount(String buyerId) {
        return (int) wishlistRepository.countByBuyerId(buyerId);
    }

    /**
     * Clear all wishlist items for a buyer
     */
    public void clearWishlist(String buyerId) {
        wishlistRepository.deleteAllByBuyerId(buyerId);
    }

    /**
     * Move wishlist item to cart (to be called from CartService)
     */
    public void removeAfterAddingToCart(String buyerId, Long productId) {
        wishlistRepository.deleteByBuyerIdAndProductId(buyerId, productId);
    }
}
