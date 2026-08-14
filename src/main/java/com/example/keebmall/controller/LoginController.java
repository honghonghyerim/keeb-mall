package com.example.keebmall.controller;

import com.example.keebmall.domain.Member;
import com.example.keebmall.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/check-username")
    @ResponseBody
    public String checkUsername(@RequestParam("username") String username) {
        boolean isDuplicate = memberService.validateDuplicateUsername(username);
        return isDuplicate ? "duplicated" : "available";
    }

    @PostMapping("/signup")
    public String signup(Member member, RedirectAttributes redirectAttributes) {
        try {
            memberService.join(member); // 여기서 서비스가 중복이면 예외 터트림!
            redirectAttributes.addFlashAttribute("signupMessage", "회원가입이 완료되었습니다!");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            // 중복이면 다시 회원가입 페이지로 보내고 에러 메시지 전달
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/signup";
        }
    }


}
