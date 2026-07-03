package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "mbr_info")
public class MemberInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mbr_Info_Id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "mbr_Id")
    private Member member;

    private String address;

    public MemberInfo(Member member, String address) {
        this.member = member;
        this.address = address;
    }

    protected MemberInfo() {

    }
}
