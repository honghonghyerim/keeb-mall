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
public class Cart {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_Id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "mbr_Id")
    private Member member;

    /*
    * 장바구니에 담긴 상품들(N)을 여기서 관리
    * mappedBy는 CartInfo 엔티티의 'cart' 필드 이름을 적어주세요.
    * Cart 는 연관관계의 주인이 아니기 때문에 CartInfo에 있는 cart 필드가 주인이니까 그걸 참고하라는 뜻
    * */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartInfo> cartInfos = new ArrayList<>();

    private LocalDateTime crtdDate = LocalDateTime.now();

    // 연관관계 편의 메서드 (이거 있으면 엄청 편합니다!)
    public void addCartInfo(CartInfo cartInfo) {
        cartInfos.add(cartInfo);
        cartInfo.setCart(this);
    }


    public Cart(Long id, Member member, List<CartInfo> cartInfos, LocalDateTime crtdDate) {
        this.id = id;
        this.member = member;
        this.cartInfos = cartInfos;
        this.crtdDate = crtdDate;
    }

    protected Cart() {

    }
}
