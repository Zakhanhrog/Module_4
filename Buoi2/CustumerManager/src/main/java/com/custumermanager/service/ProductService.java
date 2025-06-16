package com.custumermanager.service;

import com.custumermanager.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private List<Product> productsList = new ArrayList<>();

    public ProductService() {
        productsList.add(new Product(1, "Product A", 10.99));
        productsList.add(new Product(2, "Product B", 12.99));
        productsList.add(new Product(3, "Product C", 15.49));
    }

    public List<Product> getProductsList() {
        return productsList;
    }

    public Product getProductById(int productId) {
        for (Product product : productsList) {
            if (product.getId() == productId) {
                return product;
            }
        }
        return null;
    }
}
