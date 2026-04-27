package com.petshop.api.packages.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pack")
public class Pack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "customer_id")
    private Long customerId;

    @Column(nullable = false, name = "customer_name")
    private String customerName;

    @Column(nullable = false, name = "pet_id")
    private Long petId;

    @Column(nullable = false, name = "pet_name")
    private String petName;


    @OneToMany(mappedBy = "pack", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PackProtocol> protocols = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<PackProtocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<PackProtocol> protocols) {
        this.protocols = protocols;
    }
}
