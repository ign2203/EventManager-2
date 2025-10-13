package org.example.eventmanagermodule.Location;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter
@Setter
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false, unique = true, name = "name")
    private String name;

    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false, name = "address")
    private String address;

    @Min(value = 5)
    @Column(nullable = false, name = "capacity")
    private Integer capacity;

    @Column(nullable = true, name = "description")
    private String description;

    public LocationEntity() {
    }

    public LocationEntity(Long id, String name, String address, Integer capacity, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.description = description;
    }
}
