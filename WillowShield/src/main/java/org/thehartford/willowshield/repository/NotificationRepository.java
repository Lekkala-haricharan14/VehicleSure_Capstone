package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thehartford.willowshield.entity.Notification;
import org.thehartford.willowshield.entity.MyUser;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(MyUser user);
    long countByUserAndIsReadFalse(MyUser user);
}
