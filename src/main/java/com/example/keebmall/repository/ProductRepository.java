package com.example.keebmall.repository;

import com.example.keebmall.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리별 전체 검색
    List<Product> findByProdCtgCd(String prodCtgCd);

    // 카테고리 & 소분류 타입별 검색
    List<Product> findByProdCtgCdAndProdTypeCd(String prodCtgCd, String prodTypeCd);

    // 상세 조회 시 옵션(productOptions) 데이터를 함께 Fetch Join 조회
    @EntityGraph(attributePaths = {"productOptions"})
    Optional<Product> findDetailById(Long id);

}
