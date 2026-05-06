package com.be.minutemind.service;

import com.be.minutemind.dtos.request.SessionCompleteRequest;
import com.be.minutemind.dtos.request.SessionStartRequest;
import com.be.minutemind.dtos.response.ActiveSessionResponse;

public interface WorkSessionService {
    ActiveSessionResponse startSession(Long userId, SessionStartRequest request);
    void heartbeat(Long userId, Long sessionId, int currentActualMinutes);
    void completeSession(Long userId, Long sessionId, SessionCompleteRequest request);
    void discardSession(Long userId, Long sessionId);
    ActiveSessionResponse getCurrentSession(Long userId);
}
