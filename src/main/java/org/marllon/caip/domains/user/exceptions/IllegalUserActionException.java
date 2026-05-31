package org.marllon.caip.domains.user.exceptions;

import org.marllon.caip.core.exceptions.BusinessRuleException;

public class IllegalUserActionException extends BusinessRuleException {
    public IllegalUserActionException(String message) {
        super(message);
    }
}
