package org.marllon.caip.exception.user_exceptions;

import org.marllon.caip.exception.BusinessRuleException;

public class IllegalUserActionException extends BusinessRuleException {
    public IllegalUserActionException(String message) {
        super(message);
    }
}
