package org.marllon.caip.domains.user.exceptions;

import org.marllon.caip.core.exception.BusinessRuleException;

public class IllegalUserActionException extends BusinessRuleException {
    public IllegalUserActionException(String message) {
        super(message);
    }
}
