package com.hobby.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatisticsResponse {
    private long totalCustomers;
    private long activeCustomers;    // enabled = true
    private long inactiveCustomers;  // enabled = false

    //Helper Method
    public double getActivePercentage() {
        if (totalCustomers == 0) return 0;
        return (activeCustomers * 100.0) / totalCustomers;
    }
}