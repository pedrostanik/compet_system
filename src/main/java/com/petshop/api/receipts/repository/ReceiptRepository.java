package com.petshop.api.receipts.repository;

import com.petshop.api.receipts.domain.Receipt;
import com.petshop.api.receipts.domain.enums.ReceiptStatus;
import com.petshop.api.receipts.domain.enums.ReceiptType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByNumber(String number);
    List<Receipt> findByCustomerId(Long customerId);
    List<Receipt> findByType(ReceiptType type);
    List<Receipt> findByStatus(ReceiptStatus status);
    List<Receipt> findBySchedulingId(Long schedulingId);
}