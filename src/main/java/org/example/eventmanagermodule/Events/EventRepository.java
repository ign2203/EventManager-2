package org.example.eventmanagermodule.Events;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventEntity> findAllByOwnerId(Long ownerId);

    @Query("""
            SELECT eventEntity from EventEntity eventEntity
            where(:name IS NULL or eventEntity.name = :name)
            and (:placesMin IS NULL OR (eventEntity.maxPlaces - eventEntity.occupiedPlaces) >= :placesMin)
            and (:placesMax is NULL OR (eventEntity.maxPlaces - eventEntity.occupiedPlaces) <= :placesMax)
            and(:dateStartAfter IS NULL OR eventEntity.date >= :dateStartAfter)
            and(:dateStartBefore IS NULL OR eventEntity.date <= :dateStartBefore)
            and(:costMin IS NULL OR eventEntity.cost >= :costMin)
            and(:costMax IS NULL OR eventEntity.cost <= :costMax)
            and(:durationMin IS NULL OR eventEntity.duration >= :durationMin)
            and(:durationMax IS NULL OR eventEntity.duration <= :durationMax)
            and(:locationId IS NULL OR eventEntity.location.id = :locationId)
            and(:eventStatus IS NULL OR eventEntity.status = :eventStatus)
            """)
    List<EventEntity> searchEvents(
            String name,
            Integer placesMin,
            Integer placesMax,
            LocalDateTime dateStartAfter,
            LocalDateTime dateStartBefore,
            BigDecimal costMin,
            BigDecimal costMax,
            Integer durationMin,
            Integer durationMax,
            Long locationId,
            EventStatus eventStatus
    );
}
