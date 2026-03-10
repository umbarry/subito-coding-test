package com.subito.subitocodingtest.repository;

import com.subito.subitocodingtest.model.Notification;
import com.subito.subitocodingtest.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByOrderIdAndType(Long orderId, NotificationType type);
}

