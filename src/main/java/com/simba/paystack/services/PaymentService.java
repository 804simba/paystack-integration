package com.simba.paystack.services;

import com.simba.paystack.domain.request.CreatePlanRequest;
import com.simba.paystack.domain.request.InitializePaymentRequest;
import com.simba.paystack.domain.response.CreatePlanResponse;
import com.simba.paystack.domain.response.InitializePaymentResponse;
import com.simba.paystack.domain.response.PaymentVerificationResponse;

public interface PaymentService {
    CreatePlanResponse createPlan(final CreatePlanRequest createPlanRequest);

    InitializePaymentResponse initializePayment(InitializePaymentRequest initializePaymentRequest);

    PaymentVerificationResponse paymentVerification(String reference, String plan, Long customerId) throws Exception;
}
