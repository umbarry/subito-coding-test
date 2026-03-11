package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.exception.PaymentAlreadyExistsException;
import com.subito.subitocodingtest.exception.PaymentTokenException;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentService {
    @Transactional
    void processPayment(String token);
}
