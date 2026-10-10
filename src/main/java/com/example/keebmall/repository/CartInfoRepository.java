package com.example.keebmall.repository;

import com.example.keebmall.domain.CartInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartInfoRepository extends JpaRepository<CartInfo, Long> {

    // 특정 장바구니(Cart) 내에 '동일 상품 + 동일 옵션'이 이미 담겨있는지 확인
    @Query("select c from CartInfo c where c.cart.id = :cartId and c.product.id = :productId " +
            "and (:optionId is null and c.productOption is null or c.productOption.id = :optionId)")
    Optional<CartInfo> findByCartIdAndProductIdAndOptionId(
            @Param("cartId") Long cartId,
            @Param("productId") Long productId,
            @Param("optionId") Long optionId
    );

    // 특정 장바구니(Cart)의 전체 항목 조회 (Fetch Join으로 N+1 성능 이슈 방지)
    @Query("select distinct c from CartInfo c " +
            "join fetch c.product p " +
            "left join fetch c.productOption o " +
            "where c.cart.id = :cartId and c.status = 'N'")
    List<CartInfo> findAllByCartIdWithProductAndOption(@Param("cartId") Long cartId);

    // ★ 추가: 특정 장바구니(Cart) 내 활성화된(status = 'N') 항목만 조회 (Fetch Join)
    @Query("select distinct c from CartInfo c " +
            "join fetch c.product p " +
            "left join fetch c.productOption o " +
            "where c.cart.id = :cartId and c.status = :status")
    List<CartInfo> findByCartIdAndStatus(
            @Param("cartId") Long cartId,
            @Param("status") String status
    );

    // 24시간 지난 장바구니 항목 자동 정리
    @Transactional
    @Modifying
    @Query("UPDATE CartInfo c SET c.status = 'Y' WHERE c.status = 'N' AND c.crtdDate < :threshold")
    int expireOldCartItems(@Param("threshold") LocalDateTime threshold);

}