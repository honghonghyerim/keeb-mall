package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "mbr")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mbr_Id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "mbr_Psw", nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = LAZY, mappedBy = "member")
    private MemberInfo memberInfo;



//    @OneToOne(fetch = LAZY, mappedBy = "member")
//    private Order Order;
//
//    @OneToOne(fetch = LAZY, mappedBy = "member")
//    private Cart cart;
//
//    @OneToOne(fetch = LAZY, mappedBy = "member")
//    private Payment payment;
//
//    @OneToOne(fetch = LAZY, mappedBy = "member")
//    private Delivery delivery;
//
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
//    private List<OrderItem> orderItems = new ArrayList<>();

    //@JoinColumn을 쓰는 상황: "내가 외래 키(FK)를 가지고 있어!" (테이블에 컬럼이 있는 쪽)
    //
    //mappedBy를 쓰는 상황: "내 상대방이 외래 키를 가지고 있어! 나는 그냥 거울이야!" (테이블에 컬럼이 없는 쪽)

}
