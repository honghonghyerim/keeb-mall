package com.example.keebmall.repository;

import com.example.keebmall.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리별 전체 검색 - 키보드, 스위치
    List<Product> findByProdCtgCd(String prodCtgCd);

    // 키보드-기계식 / 스위치-태탁일 타입별로 검색
    List<Product> findByProdCtgCdAndProdTypeCd(String prodCtgCd, String prodTypeCd);


}
