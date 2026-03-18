package org.thehartford.willowshield.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import org.thehartford.willowshield.enums.VehicleType;
import org.thehartford.willowshield.enums.TransmissionType;

@Data
public class CreateVehicleApplicationDTO {

    @NotBlank(message = "Vehicle Owner Name is required")
    private String vehicleOwnerName;

    @NotBlank(message = "Registration Number is required")
    private String registrationNumber;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotBlank(message = "Fuel Type is required")
    private String fuelType;

    @NotBlank(message = "Chassis Number is required")
    private String chassisNumber;

    @NotNull(message = "Distance Driven is required")
    @Min(value = 0, message = "Distance driven cannot be negative")
    private Long distanceDriven;

    @NotNull(message = "Ex-Showroom Price is required")
    @Min(value = 0, message = "Ex-Showroom Price cannot be negative")
    private BigDecimal exShowroomPrice;

    @NotNull(message = "IDV is required")
    private BigDecimal idv;

    @NotNull(message = "Calculated Premium is required")
    private BigDecimal calculatedPremium;

    @NotNull(message = "Tenure Years is required")
    @Min(value = 1, message = "Tenure must be at least 1 year")
    private Integer tenureYears;

    @NotNull(message = "Vehicle Type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Plan ID is required")
    private Integer planId;

    @NotNull(message = "Transmission Type is required")
    private TransmissionType transmissionType;

    @NotNull(message = "Accidents in past is required")
    @Min(value = 0, message = "Accidents cannot be negative")
    private Integer accidentsInPast;

    public String getVehicleOwnerName() {
        return vehicleOwnerName;
    }

    public void setVehicleOwnerName(String vehicleOwnerName) {
        this.vehicleOwnerName = vehicleOwnerName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public Long getDistanceDriven() {
        return distanceDriven;
    }

    public void setDistanceDriven(Long distanceDriven) {
        this.distanceDriven = distanceDriven;
    }

    public BigDecimal getExShowroomPrice() {
        return exShowroomPrice;
    }

    public void setExShowroomPrice(BigDecimal exShowroomPrice) {
        this.exShowroomPrice = exShowroomPrice;
    }

    public BigDecimal getIdv() {
        return idv;
    }

    public void setIdv(BigDecimal idv) {
        this.idv = idv;
    }

    public BigDecimal getCalculatedPremium() {
        return calculatedPremium;
    }

    public void setCalculatedPremium(BigDecimal calculatedPremium) {
        this.calculatedPremium = calculatedPremium;
    }

    public Integer getTenureYears() {
        return tenureYears;
    }

    public void setTenureYears(Integer tenureYears) {
        this.tenureYears = tenureYears;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }

    public Integer getAccidentsInPast() {
        return accidentsInPast;
    }

    public void setAccidentsInPast(Integer accidentsInPast) {
        this.accidentsInPast = accidentsInPast;
    }
}
