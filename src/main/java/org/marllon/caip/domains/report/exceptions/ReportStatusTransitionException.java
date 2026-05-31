package org.marllon.caip.domains.report.exceptions;

import org.marllon.caip.core.exceptions.BusinessRuleException;
/**
 * Exception lançada quando há tentativa de transição inválida no ciclo de vida de um Report.
 *
 * Cenários:
 * - Tentar fechar um report já fechado
 * - Tentar vincular reports incompatíveis
 * - Tentar mudar estado de um report em status terminal (CONCLUÍDO)
 * - Tentar editar um report que não permite modificações
 */
public class ReportStatusTransitionException extends BusinessRuleException {
    public ReportStatusTransitionException(String message) {
        super(message);
    }

    public ReportStatusTransitionException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

    // Factory methods é usado pra cenarios comuns
    public static ReportStatusTransitionException cannotCloseAlreadyClosed(Long reportId) {
        return new ReportStatusTransitionException(
                "Não é possível fechar o relatório " + reportId + " pois ele já está fechado."
        );
    }

    public static ReportStatusTransitionException incompatibleReportForMatch(Long reportId, String expectedType) {
        return new ReportStatusTransitionException(
                "O relatório " + reportId + " não é compatível para vinculação. Esperado tipo: " + expectedType
        );
    }

    public static ReportStatusTransitionException cannotModifyTerminalStatus(Long reportId, String status) {
        return new ReportStatusTransitionException(
                "Não é possível modificar o relatório " + reportId + " que está no status terminal: " + status
        );
    }

    public static ReportStatusTransitionException invalidStateTransition(String currentStatus, String requestedStatus) {
        return new ReportStatusTransitionException(
                "Transição de estado inválida: " + currentStatus + " → " + requestedStatus
        );
    }
}
