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

    public Long join(Member member) {
        validateDuplicateMember(member.getUsername()); // 중복 검사
        memberRepository.save(member);
        return member.getId();
    }

    // 2. 중복 확인 (화면에서 버튼 눌렀을 때 사용)
    @Transactional(readOnly = true) // 읽기 전용이라 성능 최적화
    public void validateDuplicateMember(String username) {
        memberRepository.findByUsername(username)
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 아이디입니다.");
                });
    }


//    public void validateDuplicateMember(Member member) {
//        memberRepository.findByUsername(member.getUsername())
//                .ifPresent(m -> {
//                    throw new IllegalStateException("이미 존재하는 아이디입니다.");
//                });
//    }

}
