package org.marllon.caip.exception.reports_exceptions;

import org.marllon.caip.exception.BusinessRuleException;

public class InvalidReportMatchException extends BusinessRuleException {
    public InvalidReportMatchException(String message) {
        super(message);
    }
}
