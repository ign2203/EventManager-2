package org.example.eventmanagermodule.Location;


import jakarta.validation.Valid;
import org.example.eventmanagermodule.Location.Converter.LocationConverterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class LocationController {

    private final LocationService locationService;
    private final LocationConverterDto converter;
    private final Logger log = LoggerFactory.getLogger(LocationController.class);

    public LocationController(LocationService locationService, LocationConverterDto converter) {
        this.locationService = locationService;
        this.converter = converter;
    }


    @PostMapping("/locations")
    public ResponseEntity<LocationDto> createLocation(
            @RequestBody
            @Valid LocationDto locationDto) {
        log.info(String.format("Received request to Create Location %s", locationDto));
        var locationDomain = converter.toDomain(locationDto);

        var savedLocation = locationService.createLocation(locationDomain);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converter.toDto(savedLocation));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        log.info("Received request to get all Locations");
        var allLocation = locationService.searchAllLocation()
                .stream()
                .map(converter::toDto)
                .toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(allLocation);
    }

    @DeleteMapping("/locations/{locationId}")
    public ResponseEntity<Void> deleteLocationById(
            @PathVariable(name = "locationId") Long locationId) {

        log.info(String.format("Received request to delete Location %s", locationId));
        locationService.deleteLocation(locationId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<LocationDto> findLocationById(
            @PathVariable(name = "locationId") Long locationId) {
        log.info(String.format("Received request to find Location %s", locationId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converter.toDto(locationService.getLocationById(locationId)));
    }

    @PutMapping("/locations/{locationId}")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable(name = "locationId") Long locationId,
            @RequestBody @Valid LocationDto locationDto) {

        log.info(String.format("Received request to update Location %s", locationId));
        var updateLocationDomain = locationService.updateLocation(locationId, converter.toDomain(locationDto));


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converter.toDto(updateLocationDomain));
    }
}