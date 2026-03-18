package org.thehartford.willowshield.exceptions;

public class ClaimNotFoundException extends ResourceNotFoundException {
    public ClaimNotFoundException(Integer claimId) {
        super("Claim", "id", claimId);
    }
}
