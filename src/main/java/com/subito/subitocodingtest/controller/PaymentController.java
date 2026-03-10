package com.subito.subitocodingtest.controller;

import com.subito.subitocodingtest.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/webhook")
    public void paymentWebhook(@RequestParam("token") String token) {
        paymentService.processPayment(token);
    }
}
