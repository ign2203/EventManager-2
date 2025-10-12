package org.example.eventmanagermodule.Location;

import jakarta.validation.Valid;
import org.example.eventmanagermodule.Location.Converter.LocationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {
    private final Logger log = LoggerFactory.getLogger(LocationController.class);
    private final LocationService locationService;
    private final LocationConverter converter;

    public LocationController(LocationService locationService, LocationConverter converter) {
        this.locationService = locationService;
        this.converter = converter;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LocationDto> createLocation(
            @RequestBody
            @Valid LocationDto locationDto) {
        log.info(String.format("Received request to Create Location %s", locationDto));
        Location locationDomain = converter.fromDtoToDomain(locationDto);
        Location savedLocation = locationService.createLocation(locationDomain);
        log.info(String.format("Created Location %s (in the service layer)", savedLocation));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converter.fromDomainToDto(savedLocation));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public List<LocationDto> getAllLocations() {
        log.info("Received request to get all Locations");
        List<LocationDto> allLocation = locationService.getAllLocation()
                .stream()
                .map(converter::fromDomainToDto)
                .toList();
        return allLocation;
    }

    @DeleteMapping("/{locationId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteLocationById(
            @PathVariable(name = "locationId") Long locationId) {
        log.info(String.format("Received request to delete Location %s", locationId));
        locationService.deleteLocation(locationId);
        log.info("Deleted Location with id {} (in the service layer)", locationId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{locationId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public LocationDto getLocationById(
            @PathVariable(name = "locationId") Long locationId) {
        log.info(String.format("Received request to find Location %s", locationId));
        return converter.fromDomainToDto(locationService.getLocationById(locationId));
    }

    @PutMapping("/{locationId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable(name = "locationId") Long locationId,
            @RequestBody @Valid LocationDto locationDto) {
        log.info(String.format("Received request to update Location %s", locationId));
        Location updateLocationDomain = locationService.updateLocation(locationId, converter.fromDtoToDomain(locationDto));
        log.info("The location with the identifier {} has been successfully updated (in the service layer)", locationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converter.fromDomainToDto(updateLocationDomain));
    }
}