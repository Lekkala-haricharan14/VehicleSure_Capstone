package org.thehartford.willowshield.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStateException extends BusinessException {
    public InvalidStateException(String message) {
        super(message);
    }
}
