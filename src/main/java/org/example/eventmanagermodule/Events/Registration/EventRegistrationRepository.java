package org.example.eventmanagermodule.Events.Registration;

import org.example.eventmanagermodule.Events.EventEntity;
import org.example.eventmanagermodule.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    boolean existsByEventEntityAndUserEntity(EventEntity event, UserEntity user);

    @Query("""
            SELECT e.eventEntity FROM EventRegistration e WHERE e.userEntity = :user
            """)
    List<EventEntity> myRegisterEvent(
            UserEntity user
    );

    @Query("""
    SELECT u.userEntity FROM EventRegistration u WHERE u.eventEntity.id = :eventId
           """)
    List<UserEntity> findUsersByEventId(@Param("eventId") Long eventId);

    Optional<EventRegistration> findByEventEntityAndUserEntity(EventEntity event, UserEntity user);
}



