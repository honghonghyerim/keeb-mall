package com.example.keebmall.controller;

import com.example.keebmall.domain.Member;
import com.example.keebmall.dto.MemberResponseDto;
import com.example.keebmall.dto.MemberSignupRequestDto;
import com.example.keebmall.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class MemberController {

    private final MemberService memberService;
    /*
     * @RequestParam("username") String username) 브라우저가 @RequestParam("username")값을 자바 username 변수에 넣음
     * memberService 중복체크후 중복이면 true , 중복이 아니면 false
     *
     * */

    @PostMapping("/check-username")
    public String checkUsername(@RequestParam("username") String username) {
        boolean isDuplicate = memberService.validateDuplicateUsername(username); // true, false
        return isDuplicate ? "duplicated" : "available";
    }

    /*
    * @ModelAttribute MemberSignupRequestDto requestDto: HTML 폼에서 날아온 모든 입력값(name, username 등) DTO 에 스피링이 담음
    * */

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@ModelAttribute MemberSignupRequestDto requestDto) {
        try {
            memberService.join(requestDto); // 여기서 서비스가 중복이면 예외 터트림!
            return ResponseEntity.ok("회원가입이 완료되었습니다!");
        } catch (IllegalStateException e) {
            // 중복 등 예외 발생 시 400 Bad Request 와 함께 에러 메시지 전달
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("username") String username,
                                        @RequestParam("password") String password,
                                        HttpSession session) {

        Member loginMember = memberService.login(username, password);

        if (loginMember == null) {
            // ★ 로그인 실패 시 HTTP 401(Unauthorized) 상태 코드와 함께 에러 메시지 전달!
            // 이렇게 해야 리액트의 Axios가 catch 블록(에러)으로 들어갑니다.
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 로그인 성공! 세션에 회원 정보 저장 (세션 유지 시간 등은 기본값 사용)
        session.setAttribute("loginUser", loginMember);

        // 성공 후 메인 페이지로 이동
        return ResponseEntity.ok("로그인 성공");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getLoginUser(HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");

        if (loginUser == null) {
            return ResponseEntity.status(401).body("비로그인 상태입니다.");
        }

        // 엔티티 대신 DTO로 변환하여 응답
        return ResponseEntity.ok(new MemberResponseDto(loginUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // 세션 폭파 (로그인 상태 및 정보 제거)
        return ResponseEntity.ok("로그아웃 완료");
    }



}
