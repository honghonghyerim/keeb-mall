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

    @Column(name = "pay_No", nullable = false, unique = true)
    private String payNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private Member username;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_No", referencedColumnName = "order_No")
    private Order orderNo;

    private String payMthd;
    private int totalPayamnt;
    private String payStatus;
    private LocalDateTime payDate;

    public Payment(Long id, Member username, Order orderNo, String payMthd, int totalPayamnt, String payStatus, LocalDateTime payDate) {
        this.id = id;
        this.username = username;
        this.orderNo = orderNo;
        this.payMthd = payMthd;
        this.totalPayamnt = totalPayamnt;
        this.payStatus = payStatus;
        this.payDate = payDate;
    }

    protected Payment() {

    }
}
