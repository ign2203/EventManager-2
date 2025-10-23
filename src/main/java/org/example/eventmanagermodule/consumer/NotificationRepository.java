package org.example.eventmanagermodule.consumer;

import org.example.eventmanagermodule.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("""
            SELECT n.payload FROM NotificationEntity n WHERE n.user.id = :userId
            """)
    List<String> findAllPayloadsByUserId(Long userId);

    List<NotificationEntity> findAllByUserIdAndReadFalse(Long userId);

    List<NotificationEntity> findAllByIdInAndUserId(List<Long> notificationIds, Long userId);
}