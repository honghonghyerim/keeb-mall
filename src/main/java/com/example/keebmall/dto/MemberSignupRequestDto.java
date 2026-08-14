package com.example.keebmall.dto;

import com.example.keebmall.domain.Member;
import com.example.keebmall.domain.MemberInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MemberSignupRequestDto {

    //회원가입 DTO
    private String username;        //회원 ID
    private String password;        //회원 비밀번호
    private String name;            //회원명

    private String postcode;        //우편번호
    private String address;         //주소
    private String detailAddress;   //상세주소

    public Member toMemberEntity() {
        Member member = new Member();
        member.setUsername(this.username);
        member.setPassword(this.password);
        member.setName(this.name);
        return member;

    }
    public MemberInfo toMemberInfoEntity() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setPostcode(this.postcode);
        memberInfo.setAddress(this.address);
        memberInfo.setDetailAddress(this.detailAddress);
        return memberInfo;
    }


}
