package com.petshop.api.products.repository;

import com.petshop.api.products.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCode(String code);
    Optional<Supplier> findByCnpj(String cnpj);
    boolean existsByCode(String code);
    boolean existsByCnpj(String cnpj);
    List<Supplier> findByActiveTrue();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier.id = :supplierId")
    int countProductsBySupplierId(@Param("supplierId") Long supplierId);
}