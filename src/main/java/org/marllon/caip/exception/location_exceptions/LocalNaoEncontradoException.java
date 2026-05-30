package org.marllon.caip.exception.location_exceptions;

import org.marllon.caip.exception.global.BusinessRuleException;

public class LocalNaoEncontradoException extends BusinessRuleException {
    public LocalNaoEncontradoException(String message) {
        super(message);
    }

}
