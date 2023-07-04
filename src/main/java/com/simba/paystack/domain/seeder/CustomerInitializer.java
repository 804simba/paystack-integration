package com.simba.paystack.domain.seeder;

import com.simba.paystack.domain.Customer;
import com.simba.paystack.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerInitializer implements CommandLineRunner {
    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        Customer customer = Customer.builder().name("Oyeks Victor").build();
        customerRepository.save(customer);
    }
}
