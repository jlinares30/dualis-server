package com.dualis.api.modules.workspace.application.usecase;

import java.util.UUID;

/**
 * Port used by every module to enforce that the authenticated user belongs to a workspace
 * before reading or mutating resources scoped to it.
 */
public interface VerifyWorkspaceAccessUseCase {

    /** Email of the authenticated user (normalized to lower case). */
    String currentUserEmail();

    /** Throws ForbiddenException if the authenticated user is not a member of the workspace. */
    void assertCurrentUserIsMember(UUID workspaceId);

    /** Throws ForbiddenException if the authenticated user is not the owner of the workspace. */
    void assertCurrentUserIsOwner(UUID workspaceId);

    /** Whether the given email is a member of the workspace. */
    boolean isMember(UUID workspaceId, String email);
}
