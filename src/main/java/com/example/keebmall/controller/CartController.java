package com.example.keebmall.controller;

import com.example.keebmall.domain.Member;
import com.example.keebmall.dto.CartRequestDto;
import com.example.keebmall.dto.CartResponseDto;
import com.example.keebmall.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    // 1. 장바구니 담기 API
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequestDto requestDto, HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("회원가입 후 이용해주세요");
        }

        cartService.addToCart(requestDto, loginUser); // ★ loginUser 전달
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    // 2. 장바구니 목록 조회 API
    @GetMapping("")
    public ResponseEntity<?> getCartList(HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비로그인 상태입니다.");
        }

        List<CartResponseDto> cartList = cartService.getCartList(loginUser); // ★ loginUser 전달
        return ResponseEntity.ok(cartList);
    }

    // 3. 수량 변경
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCartCount(@PathVariable("id") Long cartInfoId,
                                             @RequestBody Map<String, Integer> request,
                                             HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비로그인 상태입니다.");
        }

        cartService.updateCartCount(cartInfoId, request.get("count"));
        return ResponseEntity.ok("수량이 변경되었습니다.");
    }

    // 4. 단일 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable("id") Long cartInfoId, HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비로그인 상태입니다.");
        }

        cartService.deleteCartItem(cartInfoId);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}