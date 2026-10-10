package com.example.keebmall.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KakaoPayService {

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.admin-key}")
    private String adminKey;

    @Value("${kakaopay.ready-url}")
    private String readyUrl;

    @Value("${kakaopay.approve-url}")
    private String approveUrl;

    // 1. 결제 준비 (Ready API)
    public Map<String, Object> prepareKakaoPay(String orderId, String userId, String itemName, int totalAmount) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        // 어드민 키 전용 Authorization 헤더 (KakaoAK {어드민키})
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("item_name", itemName);
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(totalAmount));
        params.add("tax_free_amount", "0");
        params.add("approval_url", "http://localhost:8080/api/payment/success?orderId=" + orderId);
        params.add("cancel_url", "http://localhost:8080/api/payment/cancel");
        params.add("fail_url", "http://localhost:8080/api/payment/fail");

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(readyUrl, body, Map.class);
            return response.getBody();
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("★ 카카오페이 에러 상세: " + e.getResponseBodyAsString());
            throw new RuntimeException("카카오페이 결제 준비 실패: " + e.getResponseBodyAsString(), e);
        }
    }

    // 2. 결제 승인 (Approve API)
    public Map<String, Object> approveKakaoPay(String tid, String orderId, String userId, String pgToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tid", tid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(approveUrl, body, Map.class);
            return response.getBody();
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("★ 카카오페이 승인 에러 상세: " + e.getResponseBodyAsString());
            throw new RuntimeException("카카오페이 결제 승인 실패: " + e.getResponseBodyAsString(), e);
        }
    }
}