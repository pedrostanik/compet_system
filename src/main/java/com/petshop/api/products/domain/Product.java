package com.petshop.api.products.domain;

import com.petshop.api.products.domain.enums.AnimalTarget;
import com.petshop.api.products.domain.enums.ProductCategory;
import com.petshop.api.products.domain.enums.ProductStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sku; // RAC-CAE-001

    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    private String barcode; // EAN-13 ou nulo p/ avulsos

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalTarget animalTarget;

    private String brand;

    @Column(nullable = false, length = 10)
    private String unit; // UN, KG, LT...

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(precision = 10, scale = 3)
    private BigDecimal currentStockQty = BigDecimal.ZERO;

    @Column(precision = 10, scale = 3)
    private BigDecimal minStockQty = BigDecimal.ZERO;

    private String shelfLocation; // A3-P2

    @Column(length = 10)
    private String ncm;

    @Column(nullable = false)
    private boolean loose = false; // true = produto avulso sem código de barras

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public AnimalTarget getAnimalTarget() {
        return animalTarget;
    }

    public void setAnimalTarget(AnimalTarget animalTarget) {
        this.animalTarget = animalTarget;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getCurrentStockQty() {
        return currentStockQty;
    }

    public void setCurrentStockQty(BigDecimal currentStockQty) {
        this.currentStockQty = currentStockQty;
    }

    public BigDecimal getMinStockQty() {
        return minStockQty;
    }

    public void setMinStockQty(BigDecimal minStockQty) {
        this.minStockQty = minStockQty;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
    }

    public boolean isLoose() {
        return loose;
    }

    public void setLoose(boolean loose) {
        this.loose = loose;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Calculados — não persistidos
    @Transient
    public BigDecimal getMargin() {
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return salePrice.subtract(costPrice).divide(salePrice, 4, RoundingMode.HALF_UP);
    }

    @Transient
    public boolean isBelowMinStock() {
        return currentStockQty.compareTo(minStockQty) < 0;
    }
}