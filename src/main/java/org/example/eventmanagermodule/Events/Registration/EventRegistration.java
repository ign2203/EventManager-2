package org.example.eventmanagermodule.Events.Registration;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagermodule.Events.EventEntity;
import org.example.eventmanagermodule.User.UserEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_registration",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id","user_id"}))
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    EventEntity eventEntity;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity userEntity;


    private LocalDateTime registeredAt;
    @PrePersist
    public void prePersist() { this.registeredAt = LocalDateTime.now(); }
}
