package com.petshop.api.packages.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.math.BigDecimal;

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

    @Column(name = "protocol_id", nullable = false)
    private Long protocolId;

    @Column(name = "protocol_name", nullable = false)
    private String protocolName;

    @Column(name = "protocol_price")
    private BigDecimal protocolPrice;

    @Column(name = "protocol_description", nullable = false)
    private String protocolDescription;

    // getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pack getPack() {
        return pack;
    }

    public void setPack(Pack pack) {
        this.pack = pack;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
    }

    public BigDecimal getProtocolPrice() {
        return protocolPrice;
    }

    public void setProtocolPrice(BigDecimal protocolPrice) {
        this.protocolPrice = protocolPrice;
    }

    public String getProtocolDescription() {
        return protocolDescription;
    }

    public void setProtocolDescription(String protocolDescription) {
        this.protocolDescription = protocolDescription;
    }
}