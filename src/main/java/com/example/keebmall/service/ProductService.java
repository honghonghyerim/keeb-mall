package com.example.keebmall.service;

import com.example.keebmall.domain.Product;
import com.example.keebmall.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;


    public List<Product> getProducts(String category, String type) {

        String ctgCd = convertCategoryToCode(category);

        if (type == null || type.isEmpty()) {
            return productRepository.findByProdCtgCd(ctgCd);
        }

        String typeCd = convertTypeToCode(category, type);
        return productRepository.findByProdCtgCdAndProdTypeCd(ctgCd, typeCd);

    }

    // 대분류 코드 매핑 (키보드: 1, 스위치: 2, 키캡: 3)
    private String convertCategoryToCode(String category) {
        if (category == null) return "1"; // 혹시 모를 방어 코드
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
