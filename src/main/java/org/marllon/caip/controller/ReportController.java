package org.marllon.caip.controller;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.ReportRequest;
import org.marllon.caip.dto.response.ReportResponse;
import org.marllon.caip.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reports") // Padronização da rota raiz
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ReportResponse> save(ReportRequest request) {

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(reportService.save(request).id()).toUri();

        return ResponseEntity.created(uri).body(reportService.save(request));
    }

    @PostMapping("/all-staff")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReportResponse>> findAllForStaff() {
        return ResponseEntity.ok(reportService.findAllForStaff());
    }



}
