package com.dualis.api.modules.auth.application.usecase;

import java.util.Collection;
import java.util.Map;

/** Read-only lookup of registered users used by other modules (e.g. to show partner names). */
public interface UserDirectoryUseCase {

    /** Display names (first + last name) of registered users, keyed by lower-case email. */
    Map<String, String> getDisplayNamesByEmail(Collection<String> emails);
}
