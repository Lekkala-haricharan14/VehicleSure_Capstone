package org.thehartford.willowshield.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import org.thehartford.willowshield.enums.VehicleType;
import org.thehartford.willowshield.enums.TransmissionType;

@Data
public class QuoteRequestDTO {

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotBlank(message = "Fuel Type is required")
    private String fuelType;

    @NotNull(message = "Transmission Type is required")
    private TransmissionType transmissionType;

    @NotNull(message = "Accidents in past is required")
    @Min(value = 0, message = "Accidents cannot be negative")
    private Integer accidentsInPast;

    @NotNull(message = "Ex-Showroom Price is required")
    @Min(value = 0, message = "Ex-Showroom Price cannot be negative")
    private BigDecimal exShowroomPrice;

    @NotNull(message = "Distance Driven is required")
    @Min(value = 0, message = "Distance driven cannot be negative")
    private Long distanceDriven;

    @NotNull(message = "Vehicle Type is required")
    private VehicleType vehicleType;
}
