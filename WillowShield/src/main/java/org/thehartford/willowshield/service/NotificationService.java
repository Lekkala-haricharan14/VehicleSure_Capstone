package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thehartford.willowshield.dto.NotificationDTO;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.Notification;
import org.thehartford.willowshield.repository.NotificationRepository;
import org.thehartford.willowshield.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public void createNotification(MyUser user, String message, String type) {
        Notification notification = new Notification(user, message, type);
        notificationRepository.save(notification);
    }
    
    // Explicit create for Admin(s)
    public void notifyAdmins(String message, String type) {
        userRepository.findByRole(org.thehartford.willowshield.enums.UserRole.ADMIN)
            .forEach(admin -> createNotification(admin, message, type));
    }

    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        MyUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    public long getUnreadCount(Long userId) {
        MyUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
