package org.marllon.caip.domains.location.exceptions;

import org.marllon.caip.core.exceptions.BusinessRuleException;
import org.marllon.caip.core.exceptions.ResourceNotFoundException;

public class LocalNotFoundException extends ResourceNotFoundException {
    public LocalNotFoundException(String message) {
        super(message);
    }

}
