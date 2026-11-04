package com.example.keebmall.repository;

import com.example.keebmall.domain.CartInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface CartInfoRepository extends JpaRepository<CartInfo, Long> {

    // 동일 상품 + 동일 옵션이 이미 장바구니에 있는지 확인
    @Query("select c from CartInfo c " +
            "where c.cart.id = :cartId " +
            "and c.product.id = :productId " +
            "and (:optionId is null and c.productOption is null " +
            "or c.productOption.id = :optionId)")
    Optional<CartInfo> findByCartIdAndProductIdAndOptionId(
            @Param("cartId") Long cartId,
            @Param("productId") Long productId,
            @Param("optionId") Long optionId
    );

    @Query("select distinct c from CartInfo c " +
            "join fetch c.product p " +
            "left join fetch c.productOption o " +
            "where c.cart.id = :cartId " +
            "and c.expiresAt > :now " +
            "order by p.id asc, o.id asc")
    List<CartInfo> findAllByCartIdWithProductAndOption(
            @Param("cartId") Long cartId,
            @Param("now") LocalDateTime now
    );

    // 만료된 장바구니 항목 실제 삭제
    @Modifying
    @Query("DELETE FROM CartInfo c WHERE c.expiresAt <= :now")
    int deleteExpiredCartItems(@Param("now") LocalDateTime now);

}