package com.petshop.api.products.service;

import com.petshop.api.products.domain.Product;
import com.petshop.api.products.domain.Supplier;
import com.petshop.api.products.domain.enums.ProductStatus;
import com.petshop.api.products.dto.ProductRequest;
import com.petshop.api.products.dto.ProductResponse;
import com.petshop.api.products.dto.SupplierSummary;
import com.petshop.api.products.repository.ProductRepository;
import com.petshop.api.products.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductResponse create(ProductRequest request) {
        if (request.barcode() != null && !request.barcode().isBlank()
        && productRepository.existsByBarcode(request.barcode()))
            throw new BusinessException("Código de barras já cadastrado: " + request.barcode());

        Supplier supplier = resolveSupplier(request.supplierId());

        Product product = new Product();
        product.setSku(generateSku(request));
        mapRequestToEntity(request, product, supplier);

        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(long id, ProductRequest request) {
        Product product = findOrThrow(id);

        if (request.barcode() != null
                && !request.barcode().equals(product.getBarcode())
                && productRepository.existsByBarcode(request.barcode())) {
            throw new BusinessException("Código de barras já cadastrado: " + request.barcode());
        }

        mapRequestToEntity(request, product, resolveSupplier(request.supplierId()));
        return toResponse(productRepository.save(product));
    }

    public ProductResponse findById(long id) {
        return toResponse(findOrThrow(id));
    }

    public ProductResponse findBySku(String sku) {
        return toResponse(productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + sku)));
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> search(String term) {
        return productRepository.search(term).stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> findBelowMinStock() {
        return productRepository.findBelowMinStock().stream().map(this::toResponse).toList();
    }

    public void updateStock(long id, BigDecimal qty) {
        Product product = findOrThrow(id);
        product.setCurrentStockQty(qty);
        productRepository.save(product);
    }

    public void deactivate(long id) {
        Product product = findOrThrow(id);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    // ── SKU generation ────────────────────────────────────────────────────

    private String generateSku(ProductRequest request) {
        String categoryPrefix = switch (request.category()) {
            case ROU -> "ROU";
            case ACE -> "ACE";
            case BRI -> "BRI";
            case CAM -> "CAM";
            case HIG -> "HIG";
            case ALI -> "ALI";
            default  -> "OUT";
        };

        String animalPrefix = switch (request.animalTarget()) {
            case DOG     -> "CAE";
            case CAT     -> "GAT";
            case BIRD    -> "AVE";
            case FISH    -> "PEI";
            case RODENT  -> "ROE";
            case REPTILE -> "REP";
            default      -> "ALL";
        };

        String suffix = request.loose() ? "AVU" : animalPrefix;
        String base   = categoryPrefix + "-" + suffix + "-";
        int    next   = nextSequence(base);

        return base + String.format("%03d", next);
    }

    private int nextSequence(String base) {
        return productRepository.findAll().stream()
                .map(Product::getSku)
                .filter(sku -> sku.startsWith(base))
                .mapToInt(sku -> {
                    try { return Integer.parseInt(sku.substring(base.length())); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max()
                .orElse(0) + 1;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) return null;
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado: " + supplierId));
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
    }

    private void mapRequestToEntity(ProductRequest req, Product p, Supplier supplier) {
        p.setName(req.name());
        p.setCategory(req.category());
        p.setAnimalTarget(req.animalTarget());
        p.setBrand(req.brand());
        p.setUnit(req.unit());
        p.setCostPrice(req.costPrice());
        p.setSalePrice(req.salePrice());
        p.setBarcode(req.barcode() != null && !req.barcode().isBlank() ? req.barcode() : null);
        p.setMinStockQty(req.minStockQty() != null ? req.minStockQty() : BigDecimal.ZERO);
        p.setCurrentStockQty(req.currentStockQty() != null ? req.currentStockQty() : BigDecimal.ZERO);
        p.setShelfLocation(req.shelfLocation());
        p.setNcm(req.ncm());
        p.setLoose(req.loose());
        p.setSupplier(supplier);
    }

    private ProductResponse toResponse(Product p) {
        SupplierSummary supplierSummary = p.getSupplier() == null ? null
                : new SupplierSummary(
                p.getSupplier().getId(),
                p.getSupplier().getCode(),
                p.getSupplier().getName());

        return new ProductResponse(
                p.getId(), p.getSku(), p.getName(), p.getBarcode(),
                p.getCategory(), p.getAnimalTarget(), p.getBrand(), p.getUnit(),
                p.getCostPrice(), p.getSalePrice(), p.getMargin(),
                p.getCurrentStockQty(), p.getMinStockQty(), p.isBelowMinStock(),
                p.getShelfLocation(), p.getNcm(), p.isLoose(), p.getStatus(),
                supplierSummary, p.getCreatedAt()
        );
    }
}