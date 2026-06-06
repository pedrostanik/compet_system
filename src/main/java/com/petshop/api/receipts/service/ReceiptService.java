package com.petshop.api.receipts.service;

import com.petshop.api.receipts.domain.Receipt;
import com.petshop.api.receipts.domain.ReceiptItem;
import com.petshop.api.receipts.domain.enums.ReceiptStatus;
import com.petshop.api.receipts.dto.*;
import com.petshop.api.receipts.repository.ReceiptRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PdfService pdfService;

    public ReceiptResponse create(ReceiptRequest request) {
        Receipt receipt = new Receipt();
        receipt.setNumber(generateNumber());
        receipt.setType(request.type());
        receipt.setCustomerId(request.customerId());
        receipt.setCustomerName(request.customerName());
        receipt.setPetId(request.petId());
        receipt.setPetName(request.petName());
        receipt.setSchedulingId(request.schedulingId());
        receipt.setObservations(request.observations());
        receipt.setDiscount(request.discount() != null ? request.discount() : BigDecimal.ZERO);

        List<ReceiptItem> items = request.items().stream().map(i -> {
            ReceiptItem item = new ReceiptItem();
            item.setReceipt(receipt);
            item.setDescription(i.description());
            item.setQuantity(i.quantity());
            item.setUnitPrice(i.unitPrice());
            item.setProductId(i.productId());
            item.setProductSku(i.productSku());
            return item;
        }).toList();

        receipt.setItems(items);
        return toResponse(receiptRepository.save(receipt));
    }

    public ReceiptResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<ReceiptResponse> findAll() {
        return receiptRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ReceiptResponse> findByCustomer(Long customerId) {
        return receiptRepository.findByCustomerId(customerId)
                .stream().map(this::toResponse).toList();
    }

    public ReceiptResponse markAsPaid(Long id) {
        Receipt receipt = findOrThrow(id);
        receipt.setStatus(ReceiptStatus.PAID);
        return toResponse(receiptRepository.save(receipt));
    }

    public ReceiptResponse cancel(Long id) {
        Receipt receipt = findOrThrow(id);
        receipt.setStatus(ReceiptStatus.CANCELLED);
        return toResponse(receiptRepository.save(receipt));
    }

    public byte[] generatePdf(Long id) throws Exception {
        return pdfService.generateReceipt(findOrThrow(id));
    }

    private String generateNumber() {
        int year = LocalDateTime.now().getYear();
        String prefix = "REC-" + year + "-";
        int next = receiptRepository.findAll().stream()
                .map(Receipt::getNumber)
                .filter(n -> n.startsWith(prefix))
                .mapToInt(n -> {
                    try { return Integer.parseInt(n.substring(prefix.length())); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max().orElse(0) + 1;
        return prefix + String.format("%04d", next);
    }

    private Receipt findOrThrow(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recibo não encontrado: " + id));
    }

    private ReceiptResponse toResponse(Receipt r) {
        List<ReceiptItemResponse> items = r.getItems().stream().map(i ->
                new ReceiptItemResponse(
                        i.getId(), i.getDescription(), i.getQuantity(),
                        i.getUnitPrice(), i.getTotal(),
                        i.getProductId(), i.getProductSku()
                )
        ).toList();

        return new ReceiptResponse(
                r.getId(), r.getNumber(), r.getType(), r.getStatus(),
                r.getCustomerId(), r.getCustomerName(),
                r.getPetId(), r.getPetName(),
                r.getSchedulingId(), r.getObservations(),
                r.getDiscount(), r.getSubtotal(), r.getTotal(),
                items, r.getCreatedAt()
        );
    }
}