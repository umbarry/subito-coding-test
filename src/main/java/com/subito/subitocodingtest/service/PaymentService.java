package com.subito.subitocodingtest.service;

import org.springframework.transaction.annotation.Transactional;

public interface PaymentService {
    @Transactional
    void processPayment(String token);
}
