package com.hobby.shop.util;

import com.hobby.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final OrderRepository orderRepository;
    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random RANDOM = new Random();

    public String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        String randomPart = generateRandomString(4);
        String orderNumber = PREFIX + "-" + datePart + "-" + randomPart;

        // Ensure uniqueness
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            randomPart = generateRandomString(4);
            orderNumber = PREFIX + "-" + datePart + "-" + randomPart;
        }

        return orderNumber;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}