package com.dualis.api.exception;

/**
 * Thrown when the authenticated user is not allowed to access or modify a resource
 * (e.g. the resource belongs to a workspace the user is not a member of).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
