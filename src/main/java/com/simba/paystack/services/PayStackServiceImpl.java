package com.simba.paystack.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.simba.paystack.constants.PaystackConstants;
import com.simba.paystack.domain.Customer;
import com.simba.paystack.domain.PaymentModel;
import com.simba.paystack.domain.request.CreatePlanRequest;
import com.simba.paystack.domain.request.InitializePaymentRequest;
import com.simba.paystack.domain.response.CreatePlanResponse;
import com.simba.paystack.domain.response.InitializePaymentResponse;
import com.simba.paystack.domain.response.PaymentVerificationResponse;
import com.simba.paystack.enums.PricingPlanType;
import com.simba.paystack.repository.CustomerRepository;
import com.simba.paystack.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayStackServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    @Value("${applyforme.paystack.secret.key}")
    private String paystackSecretKey;

    @Override
    public CreatePlanResponse createPlan(final CreatePlanRequest createPlanRequest) {

        CreatePlanResponse createPlanResponse = null;

        try {
            Gson gson = new Gson();

            StringEntity postingString = new StringEntity(gson.toJson(createPlanRequest));

            HttpClient client = HttpClientBuilder.create().build();

            HttpPost post = new HttpPost(PaystackConstants.PAYSTACK_INIT);

            post.setEntity(postingString);

            post.addHeader("Content-Type", "application/json");

            post.addHeader("Authorization", "Bearer " + paystackSecretKey);

            StringBuilder result = new StringBuilder();

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == PaystackConstants.STATUS_CODE_CREATED) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } else {
                throw new Exception("Paystack failed to process payment at the moment");
            }

            ObjectMapper mapper = new ObjectMapper();
            createPlanResponse = mapper.readValue(result.toString(), CreatePlanResponse.class);


        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return createPlanResponse;
    }

    @Override
    public InitializePaymentResponse initializePayment(InitializePaymentRequest initializePaymentRequest) {
        InitializePaymentResponse initializePaymentResponse = null;

        try {
            Gson gson = new Gson();

            StringEntity postingString = new StringEntity(gson.toJson(initializePaymentRequest));

            HttpClient client = HttpClientBuilder.create().build();

            HttpPost post = new HttpPost(PaystackConstants.PAYSTACK_INIT_PAYMENT);

            post.setEntity(postingString);

            post.addHeader("Content-Type", "application/json");

            post.addHeader("Authorization", "Bearer " + paystackSecretKey);

            StringBuilder result = new StringBuilder();

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == PaystackConstants.STATUS_CODE_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } else {
                throw new Exception("Paystack is unable to initialize payment now.");
            }

            ObjectMapper mapper = new ObjectMapper();
            initializePaymentResponse = mapper.readValue(result.toString(), InitializePaymentResponse.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return initializePaymentResponse;
    }

    @Override
    public PaymentVerificationResponse paymentVerification(String reference, String plan, Long customerId) throws Exception {
        if (reference.isEmpty() || plan.isEmpty()) {
            throw new Exception("Reference, plan and id must be provided in path.");
        }

        PaymentVerificationResponse paymentVerificationResponse;
        PaymentModel paymentModel = null;
        Customer customer = null;

        try {
            HttpClient client = HttpClientBuilder.create().build();

            HttpGet request = new HttpGet(PaystackConstants.PAYSTACK_VERIFY_PAYMENT + reference);

            request.addHeader("Accept", "application/json");

            request.addHeader("Authorization", "Bearer " + paystackSecretKey);

            StringBuilder result = new StringBuilder();

            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == PaystackConstants.STATUS_CODE_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } else {
                throw new Exception("Paystack is unable to verify payment at the moment");
            }

            ObjectMapper mapper = new ObjectMapper();

            paymentVerificationResponse = mapper.readValue(result.toString(), PaymentVerificationResponse.class);

            if (paymentVerificationResponse == null || paymentVerificationResponse.getData().getStatus().equals("false")) {
                throw new Exception("An error occurred while verifying payment from Paystack");
            } else if (paymentVerificationResponse.getData().getStatus().equals("success")) {
                customer = customerRepository.findById(customerId).orElseThrow(() -> new NullPointerException("Customer not found"));

                PricingPlanType pricingPlanType = PricingPlanType.valueOf(plan.toUpperCase());

                paymentModel = PaymentModel.builder()
                        .customer(customer)
                        .reference(paymentVerificationResponse.getData().getReference())
                        .amount(paymentVerificationResponse.getData().getAmount())
                        .gatewayResponse(paymentVerificationResponse.getData().getGatewayResponse())
                        .paidAt(paymentVerificationResponse.getData().getPaidAt())
                        .createdAt(paymentVerificationResponse.getData().getCreatedAt())
                        .channel(paymentVerificationResponse.getData().getChannel())
                        .currency(paymentVerificationResponse.getData().getCurrency())
                        .ipAddress(paymentVerificationResponse.getData().getIpAddress())
                        .pricingPlanType(pricingPlanType)
                        .build();
            }
        } catch (Exception e) {
            throw new Exception("Paystack Payment exception:" + e.getMessage());
        }

        paymentRepository.save(Objects.requireNonNull(paymentModel));

        return paymentVerificationResponse;
    }
}

