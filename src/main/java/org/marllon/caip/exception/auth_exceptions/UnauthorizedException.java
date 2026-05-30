package org.marllon.caip.exception.auth_exceptions;

import org.marllon.caip.exception.global.BusinessRuleException;

public class UnauthorizedException extends BusinessRuleException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
