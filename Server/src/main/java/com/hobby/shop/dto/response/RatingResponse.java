package com.hobby.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Double average;
    private Long total;
    private Map<Integer, Long> distribution;

    public static RatingResponse empty() {
        return RatingResponse.builder()
                .average(0.0)
                .total(0L)
                .distribution(Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L))
                .build();
    }
}
