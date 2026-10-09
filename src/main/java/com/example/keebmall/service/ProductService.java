package com.example.keebmall.service;

import com.example.keebmall.domain.Product;
import com.example.keebmall.dto.ProductDetailDto;
import com.example.keebmall.dto.ProductDto;
import com.example.keebmall.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;


    public List<ProductDto> getProducts(String category, String type) {

        String ctgCd = convertCategoryToCode(category); // html category url 숫자로 변환

        // 1. 전체 상품 조회 (/api/products)
        if (category == null || category.isEmpty()) {
            return productRepository.findAll().stream()
                    .map(ProductDto::new)
                    .collect(Collectors.toList());
        }

        List<Product> products;
        if (type == null || type.isEmpty()) {
            products = productRepository.findByProdCtgCd(ctgCd);
        } else {
            String typeCd = convertTypeToCode(category, type);
            products = productRepository.findByProdCtgCdAndProdTypeCd(ctgCd, typeCd);
        }


        return products.stream()
                .map(ProductDto::new)
                .collect(Collectors.toList());

    }

    // 상품 상세 조회 (옵션 포함 DTO로 변환)
    public ProductDetailDto getProdDetail(Long prodId) {
        Product product = productRepository.findDetailById(prodId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. ID: " + prodId));

        return new ProductDetailDto(product);
    }

    // 대분류 코드 매핑 (키보드: 1, 스위치: 2, 키캡: 3)
    private String convertCategoryToCode(String category) {
        if (category == null) return "1";
        switch (category.toLowerCase()) {
            case "keyboard": return "1";
            case "switch":   return "2";
            case "keycap":   return "3";
            default:         return "1";
        }
    }

    // 소분류 타입 코드 매핑
    private String convertTypeToCode(String category, String type) {
        if ("keyboard".equalsIgnoreCase(category)) {
            if ("mechanical".equalsIgnoreCase(type)) return "1-1"; // 기계식
            if ("capacitive".equalsIgnoreCase(type)) return "1-2"; // 무접점
        } else if ("switch".equalsIgnoreCase(category)) {
            if ("linear".equalsIgnoreCase(type))   return "2-1"; // 리니어
            if ("tactile".equalsIgnoreCase(type))  return "2-2"; // 택타일
        }
        return "";
    }

}
