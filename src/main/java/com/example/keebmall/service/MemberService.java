package com.example.keebmall.service;

import com.example.keebmall.domain.Member;
import com.example.keebmall.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public String join(Member member) {
        validateDuplicateUsername(member.getUsername()); // 중복 검사
        memberRepository.save(member);
        return member.getUsername();
    }

    @Transactional(readOnly = true)
    public boolean validateDuplicateUsername(String username) {
        return memberRepository.findByUsername(username).isPresent();
    }

    // 중복체크 (화면에서 가입하기 눌렀을 때 사용)
//    @Transactional(readOnly = true) // 읽기 전용이라 성능 최적화
//    public void validateDuplicateMember(String username) {
//        memberRepository.findByUsername(username)
//                .ifPresent(m -> {
//                    throw new IllegalStateException("이미 존재하는 아이디입니다.");
//                });
//    }

//    public void validateDuplicateMember(Member member) {
//        memberRepository.findByUsername(member.getUsername())
//                .ifPresent(m -> {
//                    throw new IllegalStateException("이미 존재하는 아이디입니다.");
//                });
//    }

}
