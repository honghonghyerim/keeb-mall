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

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberInfo memberInfo;

    // 👇 이 연관관계 편의 메서드를 추가해줘!
    public void setMemberInfo(MemberInfo memberInfo) {
        this.memberInfo = memberInfo;
        if (memberInfo != null) {
            memberInfo.setMember(this); // 자식 객체에도 부모(this)를 쏙 넣어줌
        }
    }


    //@JoinColumn을 쓰는 상황: "내가 외래 키(FK)를 가지고 있어!" (테이블에 컬럼이 있는 쪽)
    //
    //mappedBy를 쓰는 상황: "내 상대방이 외래 키를 가지고 있어! 나는 그냥 거울이야!" (테이블에 컬럼이 없는 쪽)

}
