package org.example.eventmanagermodule.Location;

import jakarta.persistence.EntityNotFoundException;
import org.example.eventmanagermodule.Location.Converter.LocationConverter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationConverter converter;

    public LocationService(LocationRepository locationRepository,
                           LocationConverter converter
    ) {
        this.locationRepository = locationRepository;
        this.converter = converter;
    }

    @Transactional
    public Location createLocation(Location location) {
        var searchLocation = converter.fromDomainToEntity(location);
        return converter.fromEntityToDomain(locationRepository.save(searchLocation));
    }

    public List<Location> getAllLocation() {
        return locationRepository.findAll()
                .stream()
                .map(converter::fromEntityToDomain)
                .toList();
    }

    @Transactional
    public void deleteLocation(Long locationId) {
        try {
            locationRepository.deleteById(locationId);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Location with id " + locationId + " not found", e);
        }
    }

    public Location getLocationById(Long locationId) {
        if (!locationRepository.existsById(locationId)) {

            throw new EntityNotFoundException("Location with id " + locationId + " does not exist");
        }
        return converter.fromEntityToDomain(locationRepository.findById(locationId).get());
    }

    @Transactional
    public Location updateLocation(Long locationId, Location updateNewLocation) {

        var updateEntityLocation = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location with locationId " + locationId + " does not exist"));

        updateEntityLocation.setName(updateNewLocation.name());
        updateEntityLocation.setAddress(updateNewLocation.address());
        updateEntityLocation.setCapacity(updateNewLocation.capacity());
        updateEntityLocation.setDescription(updateNewLocation.description());
        return converter.fromEntityToDomain(locationRepository.save(updateEntityLocation));
    }
}
