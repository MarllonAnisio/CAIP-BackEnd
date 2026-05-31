package org.marllon.caip.domains.report.exceptions;

import org.marllon.caip.core.exceptions.BusinessRuleException;

public class InvalidReportMatchException extends BusinessRuleException {
    public InvalidReportMatchException(String message) {
        super(message);
    }
}
