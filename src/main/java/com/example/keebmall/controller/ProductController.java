package com.example.keebmall.controller;

import com.example.keebmall.domain.Product;
import com.example.keebmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 전체, 대분류, 소분류 조회를 하나의 메서드로 커버!
    @GetMapping({"/product", "/product/{category}", "/product/{category}/{type}"})
    public String productList(@PathVariable(required = false) String category,
                              @PathVariable(required = false) String type,
                              Model model) {

        // 서비스 메서드 이름이 getProducts인지 꼭 확인하기!
        List<Product> productList = productService.getProducts(category, type);

        model.addAttribute("keyboardList", productList);
        model.addAttribute("totalCount", productList.size());
        model.addAttribute("currentCategory", category);

        return "product/keyboard"; // HTML 파일 위치가 product 폴더 안이라면 이대로 유지!
    }
}
