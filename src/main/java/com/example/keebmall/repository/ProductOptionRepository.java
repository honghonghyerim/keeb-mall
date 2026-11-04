package com.example.keebmall.repository;

import com.example.keebmall.domain.ProductOption;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    // 결제 시 옵션 재고 잠금 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM ProductOption o WHERE o.id = :optionId")
    Optional<ProductOption> findByIdForUpdate(
            @Param("optionId") Long optionId
    );
}