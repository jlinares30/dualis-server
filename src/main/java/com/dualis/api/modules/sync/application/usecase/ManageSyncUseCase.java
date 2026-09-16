package com.dualis.api.modules.sync.application.usecase;

import com.dualis.api.dto.request.SyncPullRequest;
import com.dualis.api.dto.request.SyncPushRequest;
import com.dualis.api.dto.response.SyncResponse;

public interface ManageSyncUseCase {
    SyncResponse pullDelta(SyncPullRequest request);
    SyncResponse pushOfflineData(SyncPushRequest request);
}
