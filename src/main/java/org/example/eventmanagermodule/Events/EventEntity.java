package org.example.eventmanagermodule.Events;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagermodule.Location.LocationEntity;
import org.example.eventmanagermodule.User.UserEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY) // указал ленивую загрузку, при выгрузке пользователей, их мероприятия не будут отображаться, пока их мы не попросим, обернули в прокси
    @JoinColumn(name ="user_id") // название колонки в таблице events, будет user_id,   @JoinColumn - мы говорим "соедини с другой сущностью, // соединяем с user.id
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationEntity location;


    @Column(name = "maxPlaces") //  Максимальное кол-во мест на мероприятии
    private Integer maxPlaces;

    @Column(name = "occupied_places") // Кол-во уже занятых мест
    private Integer occupiedPlaces;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "date")
    private LocalDateTime date;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "duration")
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;
}