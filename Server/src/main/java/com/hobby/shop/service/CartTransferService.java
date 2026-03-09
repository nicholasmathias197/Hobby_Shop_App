package com.hobby.shop.service;

import com.hobby.shop.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartTransferService {

    private final CartService cartService;

    @Transactional
    public void transferGuestCartToUser(String sessionId, Customer customer) {
        // Use the existing mergeCarts method
        cartService.mergeCarts(customer.getEmail(), sessionId);
    }
}