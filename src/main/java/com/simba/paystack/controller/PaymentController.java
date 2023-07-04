package com.simba.paystack.controller;

import com.simba.paystack.domain.request.CreatePlanRequest;
import com.simba.paystack.domain.request.InitializePaymentRequest;
import com.simba.paystack.domain.response.CreatePlanResponse;
import com.simba.paystack.domain.response.InitializePaymentResponse;
import com.simba.paystack.domain.response.PaymentVerificationResponse;
import com.simba.paystack.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create-plan")
    public CreatePlanResponse createPlan(@Validated @RequestBody CreatePlanRequest request) {
        return paymentService.createPlan(request);
    }

    @PostMapping("/initialize")
    public InitializePaymentResponse initializePayment(@Validated @RequestBody InitializePaymentRequest request) throws Exception {
        return paymentService.initializePayment(request);
    }


    @GetMapping("/verify/{reference}/{plan}/{customerId}")
    public PaymentVerificationResponse verifyPayment(@PathVariable("reference") final String reference,
                                                     @PathVariable("plan") final String plan,
                                                     @PathVariable("customerId") final Long customerId) throws Exception {
        return paymentService.paymentVerification(reference, plan, customerId);
    }
}
