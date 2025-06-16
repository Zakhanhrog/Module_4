package com.custumermanager.controller;

import java.util.List;
import com.custumermanager.model.Product;
import com.custumermanager.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.awt.*;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.getProductsList();
        model.addAttribute("productListAttribute", products);
        return "productList";
    }

    @GetMapping("/")
    public String homePage() {
        return "redirect:/products";
    }

    @GetMapping("/products/detail/{id}")
    public String viewProductDetail(@PathVariable("id") int productId, Model model) {
        Product product = productService.getProductById(productId);

        if (product != null) {
            model.addAttribute("productDetail", product);
            return "productDetail";
        } else {
            model.addAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + productId);
            return "errorPage";
        }
    }
}
