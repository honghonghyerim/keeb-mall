package com.example.keebmall.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "cart_info")
public class CartInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartinfo_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // CartInfo 입장에선 내 부모는 Cart!
    @JoinColumn(name = "cart_Id", nullable = false) // DB의 FK 컬럼명
    private Cart cart;

    // 어떤 상품인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_id", nullable = false)
    private Product product;

    // 선택 옵션이 없는 상품은 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private ProductOption productOption;

    // 상품 수량
    @Column(name = "count", nullable = false)
    private int count;

    // 장바구니에 처음 담은 시각
    @Column(name = "crtd_date", nullable = false)
    private LocalDateTime crtdDate;

    // 장바구니 만료 시각
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        if (this.crtdDate == null) {
            this.crtdDate = LocalDateTime.now();
        }

        if (this.expiresAt == null) {
            this.expiresAt = this.crtdDate.plusHours(24);
        }
    }

}
