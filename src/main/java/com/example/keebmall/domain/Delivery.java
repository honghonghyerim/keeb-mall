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

    @Column(name = "tracking_No", nullable = false, unique = true)
    private String trackingNo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private Member username;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "order_No", referencedColumnName = "order_No")
    private Order orderNo;

    private String address;


    @OneToOne(fetch = LAZY, mappedBy = "trackingNo")
    private DeliveryTracking deliveryTracking;
}
