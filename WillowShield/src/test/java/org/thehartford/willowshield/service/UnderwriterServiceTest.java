package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thehartford.willowshield.dto.ReadVehicleApplicationDTO;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.Vehicle;
import org.thehartford.willowshield.entity.VehicleApplication;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;
import org.thehartford.willowshield.repository.PolicyRepository;
import org.thehartford.willowshield.repository.UserRepository;
import org.thehartford.willowshield.repository.VehicleApplicationRepository;
import org.thehartford.willowshield.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
public class UnderwriterServiceTest {

    @Mock
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UnderwriterService underwriterService;

    @Test
    void getAssignedApplications_Success() {
        Long underwriterId = 1L;
        MyUser underwriter = new MyUser();
        underwriter.setId(underwriterId);

        VehicleApplication app = new VehicleApplication();
        app.setAssignedUnderwriter(underwriter);

        when(userRepository.findById(underwriterId)).thenReturn(Optional.of(underwriter));
        when(vehicleApplicationRepository.findAll()).thenReturn(List.of(app));

        List<ReadVehicleApplicationDTO> result = underwriterService.getAssignedApplications(underwriterId);

        assertEquals(1, result.size());
    }

    @Test
    void approveApplication_Success() {
        Integer appId = 1;
        Long underwriterId = 2L;

        MyUser underwriter = new MyUser();
        underwriter.setId(underwriterId);

        VehicleApplication app = new VehicleApplication();
        app.setVehicleApplicationId(appId);
        app.setStatus(VehicleApplicationStatus.ASSIGNED);
        app.setAssignedUnderwriter(underwriter);
        app.setTenureYears(1);

        when(userRepository.findById(underwriterId)).thenReturn(Optional.of(underwriter));
        when(vehicleApplicationRepository.findById(appId)).thenReturn(Optional.of(app));

        underwriterService.approveApplication(appId, underwriterId);

        assertEquals(VehicleApplicationStatus.APPROVED, app.getStatus());
        verify(vehicleRepository).save(any(Vehicle.class));
        verify(vehicleApplicationRepository).save(app);
        verify(notificationService).createNotification(eq(app.getCustomer()), anyString(), eq("POLICY_APPROVED"));
    }

    @Test
    void rejectApplication_Success() {
        Integer appId = 1;
        Long underwriterId = 2L;
        String reason = "High risk";

        MyUser underwriter = new MyUser();
        underwriter.setId(underwriterId);

        VehicleApplication app = new VehicleApplication();
        app.setVehicleApplicationId(appId);
        app.setStatus(VehicleApplicationStatus.ASSIGNED);
        app.setAssignedUnderwriter(underwriter);

        when(userRepository.findById(underwriterId)).thenReturn(Optional.of(underwriter));
        when(vehicleApplicationRepository.findById(appId)).thenReturn(Optional.of(app));

        underwriterService.rejectApplication(appId, underwriterId, reason);

        assertEquals(VehicleApplicationStatus.REJECTED, app.getStatus());
        assertEquals(reason, app.getRejectionReason());
        verify(vehicleApplicationRepository).save(app);
        verify(notificationService).createNotification(eq(app.getCustomer()), anyString(), eq("POLICY_REJECTED"));
    }
}
