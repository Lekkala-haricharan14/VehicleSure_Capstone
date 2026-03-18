package org.thehartford.willowshield.exceptions;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long userId) {
        super("User", "id", userId);
    }
    
    public UserNotFoundException(String email) {
        super("User", "email", email);
    }
}
