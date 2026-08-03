package com.dualis.api.service;

import com.dualis.api.dto.request.SyncPullRequest;
import com.dualis.api.dto.request.SyncPushRequest;
import com.dualis.api.dto.response.SyncResponse;

public interface SyncService {

    SyncResponse pullDelta(SyncPullRequest request);

    SyncResponse pushOfflineData(SyncPushRequest request);
}
