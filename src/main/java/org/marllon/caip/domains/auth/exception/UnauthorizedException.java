package org.marllon.caip.domains.auth.exception;

import org.marllon.caip.core.exception.BusinessRuleException;

public class UnauthorizedException extends BusinessRuleException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
