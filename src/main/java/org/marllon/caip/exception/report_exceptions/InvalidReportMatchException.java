package org.marllon.caip.exception.report_exceptions;

import org.marllon.caip.exception.global.BusinessRuleException;

public class InvalidReportMatchException extends BusinessRuleException {
    public InvalidReportMatchException(String message) {
        super(message);
    }
}
