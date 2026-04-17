package org.marllon.caip.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.response.LocationResponse;
import org.marllon.caip.model.Location;
import org.marllon.caip.repository.LocationRepository;
import org.marllon.caip.service.mapper.LocationMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "locations")
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    @Cacheable(key = "'all'") // Salva a lista na memória
    public List<LocationResponse> findAll() {
        return locationRepository.findAll()
                .stream()
                .map(locationMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow();

    }
}
