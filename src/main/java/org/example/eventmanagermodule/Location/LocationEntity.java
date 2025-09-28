package org.example.eventmanagermodule.Location;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "locations")
public class LocationEntity {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    Long id;


    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false, unique = true)
    String name;


    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false)
    String address;

    @Min(value = 5)
    @Column(nullable = false)
    Integer capacity;


    @Column(nullable = true)
    String description;


    public LocationEntity() {
    }

    public LocationEntity(Long id, String name, String address, Integer capacity, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}