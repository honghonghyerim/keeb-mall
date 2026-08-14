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
    @JoinColumn(name = "tracking_No", referencedColumnName = "tracking_No")
    private Delivery trackingNo;

    private String currLocation;
    private String currStatus;
    private LocalDateTime updtDate;

    public DeliveryTracking(Long id, Delivery trackingNo, String currLocation, String currStatus, LocalDateTime updtDate) {
        this.id = id;
        this.trackingNo = trackingNo;
        this.currLocation = currLocation;
        this.currStatus = currStatus;
        this.updtDate = updtDate;
    }

    protected DeliveryTracking() {

    }
}
