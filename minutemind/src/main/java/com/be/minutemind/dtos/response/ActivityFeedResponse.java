package com.be.minutemind.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityFeedResponse {
    private Long id; // ID of the activity (e.g., WorkSession ID)
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private String type; // e.g. "WORK_SESSION"
    private OffsetDateTime timestamp;
    private String content; // e.g., "hoàn thành 25 phút tập trung"
}
