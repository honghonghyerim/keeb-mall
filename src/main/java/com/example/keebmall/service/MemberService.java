package com.example.keebmall.service;

import com.example.keebmall.domain.Member;
import com.example.keebmall.domain.MemberInfo;
import com.example.keebmall.dto.MemberSignupRequestDto;
import com.example.keebmall.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void join(MemberSignupRequestDto requestDto) {
        validateDuplicateUsername(requestDto.getUsername()); // 중복 검사

        Member member = requestDto.toMemberEntity();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        member.setPassword(encodedPassword);

        MemberInfo memberInfo = requestDto.toMemberInfoEntity();

        memberInfo.setUsername(member.getUsername());

        // 3. 🚨 핵심: 편의 메서드로 부모와 자식을 양방향으로 묶어줌
        member.setMemberInfo(memberInfo);

        memberRepository.save(member);

        //디비에 등록된 회원의 아이디를 문자열로 리턴해줌
//        return member.getUsername();
    }

    /*
    * memberRepository.findByUsername(username): DB에서 해당 아이디를 가진 회원이 있는지 찾아옴
    * .isPresent(): 회원이 존재하면 true, 없으면 false를 리턴해주는 메서드
    * */
    public boolean validateDuplicateUsername(String username) {
        return memberRepository.findByUsername(username).isPresent();
    }

    public Member login(String username, String password) {
        // 1. 아이디로 회원 조회
        Member member = memberRepository.findByUsername(username)
                .orElse(null); // 없으면 null 리턴

        if (member == null) {
            return null;
        }

        // 2. 평문 비밀번호와 DB의 암호화된 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            return null; // 비밀번호 불일치
        }

        return member; // 로그인 성공 시 회원 객체 리턴
    }


}
