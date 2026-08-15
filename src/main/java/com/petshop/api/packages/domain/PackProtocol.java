package com.petshop.api.packages.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.petshop.api.protocols.domain.Protocol;
import jakarta.persistence.*;

@Entity
@Table(name = "pack_protocol")
public class PackProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    @JsonBackReference
    private Pack pack;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "protocol_id", nullable = false)
    private Protocol protocol; // join direto na entidade real

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pack getPack() { return pack; }
    public void setPack(Pack pack) { this.pack = pack; }

    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}