package com.petshop.api.receipts.controller;

import com.petshop.api.receipts.dto.ReceiptRequest;
import com.petshop.api.receipts.dto.ReceiptResponse;
import com.petshop.api.receipts.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceiptResponse create(@RequestBody ReceiptRequest request) {
        return receiptService.create(request);
    }

    @GetMapping
    public List<ReceiptResponse> findAll() {
        return receiptService.findAll();
    }

    @GetMapping("/{id}")
    public ReceiptResponse findById(@PathVariable Long id) {
        return receiptService.findById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<ReceiptResponse> findByCustomer(@PathVariable Long customerId) {
        return receiptService.findByCustomer(customerId);
    }

    @PatchMapping("/{id}/pay")
    public ReceiptResponse markAsPaid(@PathVariable Long id) {
        return receiptService.markAsPaid(id);
    }

    @PatchMapping("/{id}/cancel")
    public ReceiptResponse cancel(@PathVariable Long id) {
        return receiptService.cancel(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) throws Exception {
        byte[] pdf = receiptService.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"recibo-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}