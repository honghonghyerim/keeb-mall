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

    // 전체, 대분류, 소분류 여러개의 주소를 하나의 @GetMapping({...}) 메서드로 동시에 처리
    @GetMapping({"/product", "/product/{category}", "/product/{category}/{type}"})
    public String productList(@PathVariable(required = false) String category, //URL 경로에 포함된 category 변수를 가져오는 어노테이션
                              @PathVariable(required = false) String type,
                              Model model) { //컨트롤러에서 가공된 데이터를 담아서 뷰로 보내는 택배상자 같은 객체

        // 서비스 메서드 이름이 getProducts인지 꼭 확인하기!
        List<Product> productList = productService.getProducts(category, type); //값을 서비스계층으로 보내면서 디비에 조건에 맞는 상품목록을 조회하게함

        model.addAttribute("productList", productList);
        model.addAttribute("totalCount", productList.size()); //상품의 총개수
        model.addAttribute("currentCategory", category);

        return "product/product-list"; // HTML 파일 위치가 product 폴더 안이라면 이대로 유지!
    }

    @GetMapping("/product/detail/{id}")
    public String productDetail (@PathVariable("id") Long prod, Model model){
        Product product = productService.getProdDetail(prod);

        model.addAttribute("product", product);

        return "product/product-detail";
    }
}
