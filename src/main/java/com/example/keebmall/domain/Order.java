package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_Id")
    private Long id;

    @Column(name = "order_No", nullable = false, unique = true)
    private String orderNo;

    @ManyToOne(fetch = LAZY) // 한회원이주문을 여러개 하니까
    @JoinColumn(name = "username", referencedColumnName = "username")
    private Member username;

    private String status;
    private LocalDateTime crtdDate;

    @OneToOne(fetch = LAZY, mappedBy = "orderNo")
    private Payment payment;

    @OneToOne(fetch = LAZY, mappedBy = "orderNo")
    private Delivery delivery;

    @OneToMany(mappedBy = "orderNo", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();


}
