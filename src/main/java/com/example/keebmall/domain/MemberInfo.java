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
    @JoinColumn(name = "username", referencedColumnName = "username")
    private Member username;

    private String postcode;
    private String address;
    private String detailAddress;

}
