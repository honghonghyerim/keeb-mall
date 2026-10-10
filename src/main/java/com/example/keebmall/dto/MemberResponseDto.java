package com.example.keebmall.dto;

import com.example.keebmall.domain.Member;
import lombok.Getter;

@Getter
public class MemberResponseDto {
    private Long id;
    private String username;
    private String name;

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.name = member.getName(); // 비밀번호(password) 및 연관관계 객체는 제외!
    }
}