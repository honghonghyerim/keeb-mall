package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_Id")
    private Long id;

    // 한 회원은 여러 결제를 할 수 있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbr_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_Id", nullable = false, unique = true)
    private Order order;

    private String payMthd;
    private int totalPayamnt;
    private String payStatus;
    private LocalDateTime payDate;

    // PG사 결제 식별자
    @Column(name = "pg_pay_id", unique = true)
    private String pgPayId;

    public Payment(Long id, Member member, Order order, String payMthd, int totalPayamnt, String payStatus, LocalDateTime payDate, String pgPayId) {
        this.id = id;
        this.member = member;
        this.order = order;
        this.payMthd = payMthd;
        this.totalPayamnt = totalPayamnt;
        this.payStatus = payStatus;
        this.payDate = payDate;
        this.pgPayId = pgPayId;
    }

    public Payment() {

    }
}
