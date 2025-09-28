package com.clothsphere.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateProductDTO {

    // --- Getters & Setters ---
    @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String category;

        private String description;

        @NotNull
        @PositiveOrZero
        private Double price;

        @NotNull
        @PositiveOrZero
        private Integer stock;

        // getters & setters
        // ...

        public CreateProductDTO() {}

        public CreateProductDTO(String name, String code, String category,
                                String description, Double price, Integer stock) {
                this.name = name;
                this.code = code;
                this.category = category;
                this.description = description;
                this.price = price;
                this.stock = stock;
        }

}

