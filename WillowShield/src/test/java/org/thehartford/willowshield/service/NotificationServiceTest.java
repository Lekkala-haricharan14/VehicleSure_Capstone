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
import org.thehartford.willowshield.dto.NotificationDTO;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.Notification;
import org.thehartford.willowshield.enums.UserRole;
import org.thehartford.willowshield.repository.NotificationRepository;
import org.thehartford.willowshield.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_Success() {
        MyUser user = new MyUser();
        notificationService.createNotification(user, "Test message", "TYPE");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notifyAdmins_Success() {
        MyUser admin = new MyUser();
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(List.of(admin));

        notificationService.notifyAdmins("Admin message", "ADMIN_TYPE");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_Success() {
        Long userId = 1L;
        MyUser user = new MyUser();
        user.setId(userId);
        Notification notification = new Notification();
        notification.setMessage("Test");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(user))
            .thenReturn(List.of(notification));

        List<NotificationDTO> result = notificationService.getNotificationsForUser(userId);

        assertFalse(result.isEmpty());
        assertEquals("Test", result.get(0).getMessage());
    }

    @Test
    void markAsRead_Success() {
        Long notificationId = 1L;
        Notification notification = new Notification();
        notification.setRead(false);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notificationId);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }
}
