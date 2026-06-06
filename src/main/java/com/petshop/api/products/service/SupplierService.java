package com.petshop.api.products.service;

import com.petshop.api.products.domain.Supplier;
import com.petshop.api.products.dto.SupplierRequest;
import com.petshop.api.products.dto.SupplierResponse;
import com.petshop.api.products.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByCnpj(request.cnpj()))
            throw new BusinessException("CNPJ já cadastrado: " + request.cnpj());

        Supplier supplier = new Supplier();
        supplier.setCode(generateCode());
        mapRequestToEntity(request, supplier);

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findOrThrow(id);
        mapRequestToEntity(request, supplier);
        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }

    public void deactivate(Long id) {
        Supplier supplier = findOrThrow(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    private String generateCode() {
        int next = supplierRepository.findAll().stream()
                .map(Supplier::getCode)
                .filter(c -> c.startsWith("FOR-"))
                .mapToInt(c -> {
                    try { return Integer.parseInt(c.substring(4)); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max().orElse(0) + 1;
        return "FOR-" + String.format("%03d", next);
    }

    private Supplier findOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado: " + id));
    }

    private void mapRequestToEntity(SupplierRequest req, Supplier s) {
        s.setName(req.name());
        s.setCnpj(req.cnpj());
        s.setContactName(req.contactName());
        s.setPhone(req.phone());
        s.setEmail(req.email());
        s.setAvgLeadTimeDays(req.avgLeadTimeDays());
        s.setMinOrderQty(req.minOrderQty());
    }

    private SupplierResponse toResponse(Supplier s) {
        int totalProducts = supplierRepository.countProductsBySupplierId(s.getId());
        return new SupplierResponse(
                s.getId(), s.getCode(), s.getName(), s.getCnpj(),
                s.getContactName(), s.getPhone(), s.getEmail(),
                s.getAvgLeadTimeDays(), s.getMinOrderQty(), s.isActive(),
                totalProducts
        );
    }
}