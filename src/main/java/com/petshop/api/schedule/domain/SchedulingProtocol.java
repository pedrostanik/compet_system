package com.petshop.api.schedule.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

// schedule/domain/SchedulingProtocol.java
@Entity
@Table(name = "scheduling_protocols")
public class SchedulingProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduling_id", nullable = false)
    private Scheduling scheduling;

    @Column(name = "protocol_id", nullable = false)
    private Long protocolId;

    @Column(name = "protocol_name", nullable = false)
    private String protocolName;

    @Column(name = "protocol_price")
    private BigDecimal protocolPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Scheduling getScheduling() {
        return scheduling;
    }

    public void setScheduling(Scheduling scheduling) {
        this.scheduling = scheduling;
    }

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public BigDecimal getProtocolPrice() {
        return protocolPrice;
    }

    public void setProtocolPrice(BigDecimal protocolPrice) {
        this.protocolPrice = protocolPrice;
    }

    @Override
    public String toString() {
        return "SchedulingProtocol{" +
                "id=" + id +
                ", scheduling=" + scheduling +
                ", protocolId=" + protocolId +
                ", protocolName='" + protocolName + '\'' +
                ", protocolPrice=" + protocolPrice +
                '}';
    }
}