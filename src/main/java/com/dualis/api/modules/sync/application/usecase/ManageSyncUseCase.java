package com.dualis.api.modules.sync.application.usecase;

import com.dualis.api.modules.sync.dto.request.SyncPullRequest;
import com.dualis.api.modules.sync.dto.request.SyncPushRequest;
import com.dualis.api.modules.sync.dto.response.SyncResponse;

public interface ManageSyncUseCase {
    SyncResponse pullDelta(SyncPullRequest request);
    SyncResponse pushOfflineData(SyncPushRequest request);
}
