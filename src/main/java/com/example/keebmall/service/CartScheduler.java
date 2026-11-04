package com.example.keebmall.service;

import com.example.keebmall.repository.CartInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartScheduler {

    private final CartInfoRepository cartInfoRepository;

    // 1시간마다 만료된 장바구니 항목 삭제
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredCartItems() {

        LocalDateTime now = LocalDateTime.now();

        int deletedCount =
                cartInfoRepository.deleteExpiredCartItems(now);

        if (deletedCount > 0) {
            log.info(
                    "[장바구니 자동 정리] 만료된 장바구니 항목 {}건 삭제 완료 (기준시각: {})",
                    deletedCount, now
            );
        }
    }
}