package com.example.keebmall.scheduler;

import com.example.keebmall.repository.CartInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartScheduler {

    private final CartInfoRepository cartInfoRepository;

    // fixedRate = 3600000 -> 1시간(3,600,000 ms)마다 자동 실행
    // cron 표현식 사용 시: @Scheduled(cron = "0 0 * * * *") -> 매시간 0분에 실행
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredCartItems() {
        // 기준 시각: 현재 시각으로부터 24시간 전
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        int updatedCount = cartInfoRepository.expireOldCartItems(threshold);

        if (updatedCount > 0) {
            log.info("★ [장바구니 자동 정리] 24시간 경과한 장바구니 항목 {}건 처리 완료 (기준시각: {})", updatedCount, threshold);
        }
    }
}