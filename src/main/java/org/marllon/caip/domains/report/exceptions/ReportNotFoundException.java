package org.marllon.caip.domains.report.exceptions;

import org.marllon.caip.core.exceptions.ResourceNotFoundException;

public class ReportNotFoundException extends ResourceNotFoundException {
    public ReportNotFoundException(String message) {
        super(message);
    }
    public ReportNotFoundException(Long id) {
        super("Relatório não encontrado com o ID: " + id);
    }
}
