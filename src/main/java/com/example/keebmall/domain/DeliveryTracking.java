package com.example.keebmall.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "delivery_tracking")
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dt_Id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "dlv_Id")
    private Delivery delivery;

    private String currLocation;
    private String currStatus;
    private LocalDateTime updtDate;

    public DeliveryTracking(Long id, Delivery delivery, String currLocation, String currStatus, LocalDateTime updtDate) {
        this.id = id;
        this.delivery = delivery;
        this.currLocation = currLocation;
        this.currStatus = currStatus;
        this.updtDate = updtDate;
    }

    protected DeliveryTracking() {

    }
}
