package com.example.keebmall.controller;

import com.example.keebmall.domain.Product;
import com.example.keebmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProductController {

    private final ProductService productService;

    // 전체, 대분류, 소분류 여러개의 주소를 하나의 @GetMapping({...}) 메서드로 동시에 처리
    @GetMapping({"", "/{category}", "/{category}/{type}"})
    public ResponseEntity<Map<String, Object>> getProducts(
            @PathVariable(value = "category",required = false) String category, //URL 경로에 포함된 category 변수를 가져오는 어노테이션
            @PathVariable(value = "type",required = false) String type) {

        // 서비스 메서드 이름이 getProducts인지 꼭 확인하기!
        List<Product> productList = productService.getProducts(category, type); //값을 서비스계층으로 보내면서 디비에 조건에 맞는 상품목록을 조회하게함

        Map<String, Object> response = new HashMap<>();
        response.put("productList", productList);      // ProductList.jsx 의 data.productList와 매핑
        response.put("totalCount", productList.size()); // ProductList.jsx 의 data.totalCount와 매핑
        response.put("currentCategory", category);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/detail/{id}")
    public String productDetail (@PathVariable("id") Long prod, Model model){
        Product product = productService.getProdDetail(prod);

        model.addAttribute("product", product);

        return "product/product-detail";
    }

    // 2. 상품 상세 정보 조회 API
    // GET /api/products/detail/1
    @GetMapping("/detail/{id}")
    public ResponseEntity<Product> getProductDetail(@PathVariable("id") Long prod) {

        // 기존 서비스 메서드 이름 getProdDetail(prod) 그대로 사용!
        Product product = productService.getProdDetail(prod);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }
}
