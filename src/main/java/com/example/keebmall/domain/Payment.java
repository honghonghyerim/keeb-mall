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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbr_Id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_Id")
    private Order order;

    private String payMthd;
    private int totalPayamnt;
    private String payStatus;
    private LocalDateTime payDate;

    public Payment(Long id, Member member, Order order, String payMthd, int totalPayamnt, String payStatus, LocalDateTime payDate) {
        this.id = id;
        this.member = member;
        this.order = order;
        this.payMthd = payMthd;
        this.totalPayamnt = totalPayamnt;
        this.payStatus = payStatus;
        this.payDate = payDate;
    }

    protected Payment() {

    }
}
