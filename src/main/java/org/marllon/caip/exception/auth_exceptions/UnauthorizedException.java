package org.marllon.caip.exception.auth_exceptions;

import org.marllon.caip.exception.BusinessRuleException;

public class UnauthorizedException extends BusinessRuleException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
