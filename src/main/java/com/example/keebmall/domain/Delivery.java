package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dlv_Id")
    private Long id;

//    @Column(name = "tracking_No", nullable = false, unique = true)
//    private String trackingNo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "mbr_id", nullable = false)
    private Member member;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "order_Id", nullable = false)
    private Order order;

    private String address;


    @OneToOne(fetch = LAZY, mappedBy = "delivery")
    private DeliveryTracking deliveryTracking;
}
