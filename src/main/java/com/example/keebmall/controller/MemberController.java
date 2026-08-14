package com.example.keebmall.controller;

import com.example.keebmall.dto.MemberSignupRequestDto;
import com.example.keebmall.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 화면보여주기
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    /*
     * @ResponseBody: 메서드가 리턴하는 문자열을 순수한텍스트로 보내줌 (화면을 이동시키지 않고 데이터만 던져줄때 필수)
     * @RequestParam("username") String username) 브라우저가 @RequestParam("username")값을 자바 username 변수에 넣음
     * memberService 중복체크후 중복이면 true , 중복이 아니면 false
     *
     * */

    @PostMapping("/check-username")
    @ResponseBody
    public String checkUsername(@RequestParam("username") String username) {
        boolean isDuplicate = memberService.validateDuplicateUsername(username); // true, false
        return isDuplicate ? "duplicated" : "available";
    }

    /*
    * @ModelAttribute MemberSignupRequestDto requestDto: HTML 폼에서 날아온 모든 입력값(name, username 등) DTO 에 스피링이 담음
    * RedirectAttributes redirectAttributes: 페이지를 다른 곳으로 리다이렉트(redirect)할 때, 다음 페이지에 딱 한 번만 쓸 수 있는 일회성 메시지(데이터)를 보여줌
    * 에러는 e 라는 변수에 담김
    * "errorMessage", e.getMessage() / 키, 벨류
    * */

    @PostMapping("/signup")
    public String signup(@ModelAttribute MemberSignupRequestDto requestDto, RedirectAttributes redirectAttributes) {
        try {
            memberService.join(requestDto); // 여기서 서비스가 중복이면 예외 터트림!
            redirectAttributes.addFlashAttribute("signupMessage", "회원가입이 완료되었습니다!");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            // 중복이면 다시 회원가입 페이지로 보내고 에러 메시지 전달
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/signup";
        }
    }


}
