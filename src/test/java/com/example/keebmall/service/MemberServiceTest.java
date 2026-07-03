package com.example.keebmall.service;

import com.example.keebmall.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;

    @Test
    void 회원가입_테스트() {

        Member member = new Member();
        member.setUsername("gpfla42003");
        member.setPassword("1234");
        member.setName("혜림");
        memberService.join(member);

        System.out.println("가입된 아이디: " + member.getUsername());
        System.out.println("회원가입에 성공했습니다!");

    }

    @Test
    void 아이디중복_테스트() {

        Member member = new Member();
        member.setUsername("gpfla42004");
        member.setPassword("789");
        member.setName("혜진");
        memberService.join(member);

        try {
            memberService.validateDuplicateMember("gpfla42004");
        } catch (IllegalStateException e) {
            System.out.println("중복 테스트 결과: " + e.getMessage());
            return; // 정상 종료
        }
        // 여기까지 오면 테스트 실패
        fail("중복인데 왜 에러가 안 나지?");

    }

}