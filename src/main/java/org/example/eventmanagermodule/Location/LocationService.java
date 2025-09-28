package org.example.eventmanagermodule.Location;

import jakarta.persistence.EntityNotFoundException;
import org.example.eventmanagermodule.Location.Converter.LocationConverterEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationConverterEntity converterEntity;


    public LocationService(LocationRepository locationRepository, LocationConverterEntity converterEntity) {// вот здесь подчеркивается converterEntity, IDEA ругается
        this.locationRepository = locationRepository;
        this.converterEntity = converterEntity;
    }

    @Transactional
    public Location createLocation(Location location) {
        var searchLocation = converterEntity.toEntity(location);
        if (locationRepository.existsByName(searchLocation.getName())) {
            throw new IllegalArgumentException("Location with name " + searchLocation.getName() + " already exists");
        }
        return converterEntity.toDomain(locationRepository.save(searchLocation));
    }


    public List<Location> searchAllLocation() {
        return locationRepository.findAll()
                .stream()
                .map(converterEntity::toDomain)
                .toList();
    }

    @Transactional
    public void deleteLocation(Long locationId) {

        if (!locationRepository.existsById(locationId)) {
            throw new EntityNotFoundException("Location with id " + locationId + " does not exist");
        }
        locationRepository.deleteById(locationId);
    }


    public Location getLocationById(Long locationId) {
        if (!locationRepository.existsById(locationId)) {

            throw new EntityNotFoundException("Location with id " + locationId + " does not exist");
        }
        return converterEntity.toDomain(locationRepository.findById(locationId).get());
    }

    @Transactional
    public Location updateLocation(Long locationId, Location updateNewLocation) {
        if (!locationRepository.existsById(locationId)) {

            throw new EntityNotFoundException("Location with id " + locationId + " does not exist");
        }

        locationRepository.updateLocation(
                locationId,
                updateNewLocation.name(),
                updateNewLocation.address(),
                updateNewLocation.capacity(),
                updateNewLocation.description()
        );
        return converterEntity.toDomain(locationRepository.findById(locationId).get());

    }
}