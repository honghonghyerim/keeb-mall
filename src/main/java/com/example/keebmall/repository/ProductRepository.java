package com.example.keebmall.repository;

import com.example.keebmall.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리별 전체 검색
    List<Product> findByProdCtgCd(String prodCtgCd);

    // 카테고리 & 소분류 타입별 검색
    List<Product> findByProdCtgCdAndProdTypeCd(String prodCtgCd, String prodTypeCd);

    // 상세 조회 시 옵션 데이터 함께 조회
    @EntityGraph(attributePaths = {"productOptions"})
    Optional<Product> findDetailById(Long id);

    // 결제 시 상품 재고 잠금 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdForUpdate(
            @Param("productId") Long productId
    );

}
