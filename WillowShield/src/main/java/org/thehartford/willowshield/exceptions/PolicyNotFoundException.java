package org.thehartford.willowshield.exceptions;

public class PolicyNotFoundException extends ResourceNotFoundException {
    public PolicyNotFoundException(Integer policyId) {
        super("Policy", "id", policyId);
    }
}
