package org.marllon.caip.domains.auth.exceptions;

import org.marllon.caip.core.exceptions.BusinessRuleException;

public class UnauthorizedException extends BusinessRuleException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
