package com.example.keebmall.repository;

import com.example.keebmall.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPgPayId(String  pgPayId);

}


