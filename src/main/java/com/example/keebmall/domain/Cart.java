package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter @Setter
public class Cart {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_Id")
    private Long id;

    // 회원 한 명당 장바구니 하나
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbr_id", nullable = false, unique = true)
    private Member member;

    /*
    * 장바구니에 담긴 상품들(N)을 여기서 관리
    * mappedBy는 CartInfo 엔티티의 'cart' 필드 이름을 적어주세요.
    * Cart 는 연관관계의 주인이 아니기 때문에 CartInfo에 있는 cart 필드가 주인이니까 그걸 참고하라는 뜻
    * */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartInfo> cartInfos = new ArrayList<>();


    // 연관관계 편의 메서드
    public void addCartInfo(CartInfo cartInfo) {
        cartInfos.add(cartInfo);
        cartInfo.setCart(this);
    }

    public  Cart() {
    }
}
