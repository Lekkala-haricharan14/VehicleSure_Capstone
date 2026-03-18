package org.thehartford.willowshield.exceptions;

public class ApplicationNotFoundException extends ResourceNotFoundException {
    public ApplicationNotFoundException(Integer appId) {
        super("Application", "id", appId);
    }
}
