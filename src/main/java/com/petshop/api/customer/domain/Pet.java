package com.petshop.api.customer.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.petshop.api.customer.domain.enums.CoatType;
import com.petshop.api.customer.domain.enums.SpecieType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "age")
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpecieType species;

    @Column(nullable = false, name = "race")
    private String race;

    @Column(name="rabie_vaccination")
    private Boolean rabieVaccination;

    @Column(name="rabie_vaccination_date")
    private LocalDate rabieVaccinationDate;

    @Column(name="v10_vaccination")
    private Boolean v10Vaccination;

    @Column(name="v10_vaccination_date")
    private LocalDate v10VaccinationDate;

    @Column(name="dewormed")
    private Boolean dewormed;

    @Column(name="dewormed_date")
    private LocalDate dewormedDate;

    @Column(name="allergy")
    private String allergy;

    @Column(name="health_issues")
    private String healthIssues;

    @Column(name="weight")
    private Float weight;

    @Enumerated(EnumType.STRING)
    @Column(name="coat_type")
    private CoatType coatType;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "customer_id")
    private Customer customer;

    @Column(name="obs")
    private String observations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public SpecieType getSpecies() {
        return species;
    }

    public void setSpecies(SpecieType species) {
        this.species = species;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }


    public Boolean getRabieVaccination() {
        return rabieVaccination;
    }

    public void setRabieVaccination(Boolean rabieVaccination) {
        this.rabieVaccination = rabieVaccination;
    }

    public LocalDate getRabieVaccinationDate() {
        return rabieVaccinationDate;
    }

    public void setRabieVaccinationDate(LocalDate rabieVaccinationDate) {
        this.rabieVaccinationDate = rabieVaccinationDate;
    }

    public Boolean getV10Vaccination() {
        return v10Vaccination;
    }

    public void setV10Vaccination(Boolean v10Vaccination) {
        this.v10Vaccination = v10Vaccination;
    }

    public LocalDate getV10VaccinationDate() {
        return v10VaccinationDate;
    }

    public void setV10VaccinationDate(LocalDate v10VaccinationDate) {
        this.v10VaccinationDate = v10VaccinationDate;
    }

    public Boolean getDewormed() {
        return dewormed;
    }

    public void setDewormed(Boolean dewormed) {
        this.dewormed = dewormed;
    }

    public LocalDate getDewormedDate() {
        return dewormedDate;
    }

    public void setDewormedDate(LocalDate dewormedDate) {
        this.dewormedDate = dewormedDate;
    }

    public String getAllergy() {
        return allergy;
    }

    public void setAllergy(String allergy) {
        this.allergy = allergy;
    }

    public String getHealthIssues() {
        return healthIssues;
    }

    public void setHealthIssues(String healthIssues) {
        this.healthIssues = healthIssues;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public CoatType getCoatType() {
        return coatType;
    }

    public void setCoatType(CoatType coatType) {
        this.coatType = coatType;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", species='" + species + '\'' +
                ", race='" + race + '\'' +
                ", rabieVaccination=" + rabieVaccination +
                ", rabieVaccinationDate=" + rabieVaccinationDate +
                ", v10Vaccination=" + v10Vaccination +
                ", v10VaccinationDate=" + v10VaccinationDate +
                ", dewormed=" + dewormed +
                ", dewormedDate=" + dewormedDate +
                ", allergy='" + allergy + '\'' +
                ", healthIssues='" + healthIssues + '\'' +
                ", weight=" + weight +
                ", coatType='" + coatType + '\'' +
                ", customer=" + customer +
                ", observations='" + observations + '\'' +
                '}';
    }
}
