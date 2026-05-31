package org.marllon.caip.domains.location.exceptions;

import org.marllon.caip.core.exception.BusinessRuleException;

public class LocalNaoEncontradoException extends BusinessRuleException {
    public LocalNaoEncontradoException(String message) {
        super(message);
    }

}
