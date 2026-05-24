package com.be.minutemind.dtos.response;

import com.be.minutemind.enums.InvitationStatus;

import java.time.OffsetDateTime;

public record GoalInvitationResponse(
        Long id,
        Long goalId,
        String goalTitle,
        Long inviterId,
        String inviterName,
        String inviterAvatarUrl,
        Long inviteeId,
        String inviteeName,
        InvitationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime respondedAt
) {
}
