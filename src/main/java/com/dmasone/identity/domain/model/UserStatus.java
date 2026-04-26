package com.dmasone.identity.domain.model;

/**
 * Lifecycle status of a user account.
 *
 * <p>The enum supports soft deletes through {@link #INACTIVE} while preserving
 * the record for auditability and future business workflows.</p>
 */
public enum UserStatus {
    /**
     * User can access active identity-related workflows.
     */
    ACTIVE,

    /**
     * User has been soft-deleted or disabled.
     */
    INACTIVE,

    /**
     * User is temporarily blocked by an administrative or policy decision.
     */
    SUSPENDED
}
