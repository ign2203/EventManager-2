package dev.sorokin.eventnotificator.consumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAllByUserIdAndReadFalse(Long userId);

    List<NotificationEntity> findAllByIdInAndUserId(List<Long> notificationIds, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationEntity n WHERE n.createdAt < :threshold")
    void deleteAllOlderThan(@Param("threshold") LocalDateTime threshold);
}