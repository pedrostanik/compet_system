package com.petshop.api.schedule.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedulings")
public class Scheduling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name="happened")
    private Boolean scheduledHappened;

    @Column(name="is_package")
    private Boolean isPackage;

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

    public Boolean getScheduledHappened() {
        return scheduledHappened;
    }

    public void setScheduledHappened(Boolean scheduledHappened) {
        this.scheduledHappened = scheduledHappened;
    }

    public Boolean getPackage() {
        return isPackage;
    }

    public void setPackage(Boolean aPackage) {
        isPackage = aPackage;
    }

    @Override
    public String toString() {
        return "Scheduling{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", petId=" + petId +
                ", petName='" + petName + '\'' +
                ", schedulingObservations='" + schedulingObservations + '\'' +
                ", scheduledTime='" + time + '\'' +
                ", scheduledHappened=" + scheduledHappened +
                ", isPackage=" + isPackage +
                '}';
    }
}
