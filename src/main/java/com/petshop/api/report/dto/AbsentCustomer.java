package com.petshop.api.report.dto;

import com.petshop.api.customer.domain.Customer;

public record AbsentCustomer(int days, Long customerId, String customerName, Long schedulingId) {
}
