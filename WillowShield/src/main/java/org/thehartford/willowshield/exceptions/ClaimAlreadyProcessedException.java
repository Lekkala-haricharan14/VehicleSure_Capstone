package org.thehartford.willowshield.exceptions;

public class ClaimAlreadyProcessedException extends InvalidStateException {
    public ClaimAlreadyProcessedException(Integer claimId, String status) {
        super("Claim with ID " + claimId + " has already been processed. Current status: " + status);
    }
}
