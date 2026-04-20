package org.marllon.caip.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.marllon.caip.controller.doc.ReportControllerDoc;
import org.marllon.caip.dto.request.ReportRequest;
import org.marllon.caip.dto.response.ReportResponse;
import org.marllon.caip.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController implements ReportControllerDoc {

    private final ReportService reportService;

    /**
     *  Rotas publicas para uso de alunos logados
     * */

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> findById(Long id) {
        return ResponseEntity.ok(reportService.findById(id));
    }

    @GetMapping("/my-active-reports")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        return ResponseEntity.ok(reportService.findMyActiveReports());
    }

    @GetMapping("/my-reports")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReportResponse>> findMyReports() {
        return ResponseEntity.ok(reportService.findMyReports());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();

    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ReportResponse> save(ReportRequest request) {

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(reportService.save(request).id()).toUri();

        return ResponseEntity.created(uri).body(reportService.save(request));
    }

    /**
     * Rotas privadas
     * */

    @GetMapping("/all-staff")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReportResponse>> findAllForStaff() {
        return ResponseEntity.ok(reportService.findAllForStaff());
    }

    @GetMapping("/all-active")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReportResponse>> findAllActive() throws Exception {
        return ResponseEntity.ok(reportService.findAllActive());
    }

    @GetMapping("/all-closed")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReportResponse>> findAllClosed() throws Exception {
        return ResponseEntity.ok(reportService.findClosedReports());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ReportResponse> update(@PathVariable Long id, @RequestBody @Valid ReportRequest request) {
        return ResponseEntity.ok(reportService.update(id, request));
    }

    @PostMapping("/link-reports")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ReportResponse> linkReports(
            @RequestParam Long perdidoId,
            @RequestParam Long encontradoId
    ) {
        return ResponseEntity.ok(reportService.linkReports(perdidoId, encontradoId));
    }

    @DeleteMapping("/hard-delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id){
        reportService.hardDeleteReport(id);
        return ResponseEntity.noContent().build();
    }

}
