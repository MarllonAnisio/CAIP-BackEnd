package org.marllon.caip.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.model.Location;
import org.marllon.caip.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LocationService {
    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public List<Location> findAll() {
        return locationRepository.findAll();
    }
}
