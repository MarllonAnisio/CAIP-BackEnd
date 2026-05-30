package org.marllon.caip.service.impl;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.LocationRequest;
import org.marllon.caip.dto.response.LocationResponse;
import org.marllon.caip.exception.location_exceptions.LocalJaCadastradoException;
import org.marllon.caip.exception.location_exceptions.LocalNaoEncontradoException;
import org.marllon.caip.model.entity.Location;
import org.marllon.caip.repository.LocationRepository;
import org.marllon.caip.service.mapper.LocationMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Cacheable(key = "#id")
    public Location findEntityById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocalNaoEncontradoException("Localização não encontrada com ID: " + id));
    }


    @Transactional(readOnly = true)
    public LocationResponse findById(Long id) {
        Location location = findEntityById(id);
        return locationMapper.toResponse(location);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    @CacheEvict(allEntries = true)// isso é usado porque quando trabalhamos com cache, em toda atualizão e criação de um objeto, devemos limpar a cache.
    public LocationResponse create(LocationRequest request) {

        if (locationRepository.existsByNameIgnoreCase(request.name())) {

            throw new LocalJaCadastradoException("Local já existe no Sistema");
        }

        Location location = new Location();
        location.setName(request.name());

        return locationMapper.toResponse(locationRepository.save(location));
    }


    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    @CacheEvict(allEntries = true)
    public LocationResponse update(Long id, LocationRequest request) {
        Location location = findEntityById(id);

        if (!location.getName().equalsIgnoreCase(request.name()) &&
                locationRepository.existsByNameIgnoreCase(request.name())) {
            throw new LocalJaCadastradoException("Já existe outra localização com este nome.");
        }

        location.setName(request.name());

        return locationMapper.toResponse(locationRepository.save(location));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        Location location = findEntityById(id);
        locationRepository.delete(location);
    }
}
