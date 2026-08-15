package com.petshop.api.schedule.domain;

import com.petshop.api.schedule.domain.enums.ScheduleStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedulings")
public class Scheduling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="pack_id")
    private Long packId;

    @Column(name="pack_cycle")
    private Integer packCycle;

    @Column(nullable = false, name = "customer_id")
    private Long customerId;

    @Column(name="customer_name")
    private String customerName;

    @JoinColumn(name="pet_id")
    private Long petId;

    @Column(name="pet_name")
    private String petName;

    @Column(name="observations")
    private String schedulingObservations;

    @Column(name="time")
    private LocalDateTime time;

    @Enumerated(EnumType.STRING)
    @Column(name="schedule_status")
    private ScheduleStatus scheduleStatus;

    @Column(name="is_package")
    private Boolean isPackage;

//    @Column(name = "package_cycle_id")
//    private Long packageCycleId;

    @Column(name="duration")
    private Integer duration;

    @Column(name="intercepted")
    private Boolean intercepted;

    @Column(name="price")
    private BigDecimal price;

    @OneToMany(mappedBy = "scheduling", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchedulingProtocol> protocols = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSchedulingObservations() {
        return schedulingObservations;
    }

    public void setSchedulingObservations(String schedulingObservations) {
        this.schedulingObservations = schedulingObservations;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public ScheduleStatus getScheduleStatus() {
        return scheduleStatus;
    }

    public void setScheduleStatus(ScheduleStatus scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }

    public Boolean getPackage() {
        return isPackage;
    }

    public void setPackage(Boolean aPackage) {
        isPackage = aPackage;
    }

    public List<com.petshop.api.schedule.domain.SchedulingProtocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<com.petshop.api.schedule.domain.SchedulingProtocol> protocols) {
        this.protocols = protocols;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getIntercepted() {
        return intercepted;
    }

    public void setIntercepted(Boolean intercepted) {
        this.intercepted = intercepted;
    }

    public Long getPackId() {
        return packId;
    }

    public void setPackId(Long packId) {
        this.packId = packId;
    }

    public Integer getPackCycle() {
        return packCycle;
    }

    public void setPackCycle(Integer packCycle) {
        this.packCycle = packCycle;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
