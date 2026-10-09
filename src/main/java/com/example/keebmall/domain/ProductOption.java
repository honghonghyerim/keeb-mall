package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prodopt_Id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "prod_id")
    @ToString.Exclude
    private Product product;

    private String kbdColor;
    private String kbdLayout;

    private int stock;         // 옵션별 개별 재고

    public ProductOption() {

    }
}
