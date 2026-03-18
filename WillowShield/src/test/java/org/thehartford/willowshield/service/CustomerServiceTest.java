package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.thehartford.willowshield.dto.CreateVehicleApplicationDTO;
import org.thehartford.willowshield.dto.QuoteRequestDTO;
import org.thehartford.willowshield.dto.QuoteResponseDTO;
import org.thehartford.willowshield.entity.*;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;
import org.thehartford.willowshield.enums.VehicleType;
import org.thehartford.willowshield.enums.RiskLevel;
import org.thehartford.willowshield.enums.TransmissionType;
import org.thehartford.willowshield.repository.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PolicyPlanRepository policyPlanRepository;

    @Mock
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Mock
    private VehicleDocumentRepository vehicleDocumentRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customerService, "uploadDir", "uploads/documents/");
    }

    @Test
    void submitVehicleApplication_Success() throws IOException {
        Long customerId = 1L;
        CreateVehicleApplicationDTO dto = new CreateVehicleApplicationDTO();
        dto.setPlanId(1);
        dto.setRegistrationNumber("DL01AB1234");
        dto.setChassisNumber("ABC12345678901234"); // 17 chars
        dto.setYear(2022);
        dto.setDistanceDriven(5000L);
        dto.setVehicleType(VehicleType.CAR);
        dto.setExShowroomPrice(BigDecimal.valueOf(800000));
        dto.setIdv(BigDecimal.valueOf(700000));
        dto.setCalculatedPremium(BigDecimal.valueOf(15000));
        dto.setTenureYears(1);
        dto.setTransmissionType(TransmissionType.MANUAL);
        dto.setAccidentsInPast(0);
        dto.setFuelType("PETROL");
        dto.setVehicleOwnerName("John Doe");
        dto.setMake("Toyota");
        dto.setModel("Camry");

        MyUser customer = new MyUser();
        customer.setId(customerId);

        PolicyPlan plan = new PolicyPlan();
        plan.setPlanId(1);
        plan.setActive(true);
        plan.setApplicableVehicleType(VehicleType.CAR);

        VehicleApplication savedApp = new VehicleApplication();
        savedApp.setVehicleApplicationId(1);

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(policyPlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(vehicleApplicationRepository.existsByRegistrationNumberAndStatusNot(anyString(), any())).thenReturn(false);
        when(vehicleApplicationRepository.save(any(VehicleApplication.class))).thenReturn(savedApp);

        MockMultipartFile rcFile = new MockMultipartFile("rcFile", "rc.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile invoiceFile = new MockMultipartFile("invoiceFile", "invoice.pdf", "application/pdf", "content".getBytes());

        customerService.submitVehicleApplication(customerId, dto, rcFile, invoiceFile);

        verify(vehicleApplicationRepository).save(any(VehicleApplication.class));
        verify(vehicleDocumentRepository).save(any(VehicleDocument.class));
        verify(notificationService).notifyAdmins(anyString(), eq("NEW_APPLICATION_SUBMITTED"));
    }

    @Test
    void submitVehicleApplication_InvalidRegistration_ThrowsException() {
        CreateVehicleApplicationDTO dto = new CreateVehicleApplicationDTO();
        dto.setRegistrationNumber("INVALID");

        assertThrows(BusinessException.class, () -> customerService.submitVehicleApplication(1L, dto, null, null));
    }

    @Test
    void calculateRiskLevel_LowRisk() {
        RiskLevel risk = customerService.calculateRiskLevel(2, 0, 5000L);
        assertEquals(RiskLevel.LOW, risk);
    }

    @Test
    void generateQuotes_Success() {
        QuoteRequestDTO request = new QuoteRequestDTO();
        request.setYear(2022);
        request.setExShowroomPrice(BigDecimal.valueOf(1000000));
        request.setVehicleType(VehicleType.CAR);
        request.setAccidentsInPast(0);
        request.setDistanceDriven(5000L);

        PolicyPlan plan = new PolicyPlan();
        plan.setPlanId(1);
        plan.setPlanName("Basic Plan");
        plan.setBasePremium(BigDecimal.valueOf(10000));
        plan.setPolicyType("Comprehensive");
        plan.setActive(true);
        plan.setApplicableVehicleType(VehicleType.CAR);

        when(policyPlanRepository.findByApplicableVehicleTypeAndIsActiveTrue(VehicleType.CAR)).thenReturn(List.of(plan));

        List<QuoteResponseDTO> result = customerService.generateQuotes(request);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Basic Plan", result.get(0).getPlanName());
    }
}
