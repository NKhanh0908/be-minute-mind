# Community & Shared Goals API

Handles user connections (following), leaderboard queries, activity feeds, and collaborative goal sharing.

All endpoints in this section are prefixed with `/community` and require a valid JWT Bearer Token.

---

## 1. Social Graphs & Feeds

### Follow User
- **URL**: `/follow/{followingId}`
- **Method**: `POST`
- **Response**: `24 No Content`

### Unfollow User
- **URL**: `/unfollow/{followingId}`
- **Method**: `DELETE`
- **Response**: `204 No Content`

### Get Daily Leaderboard
Retrieves the daily ranking of the user and followed friends based on today's focus minutes.
- **URL**: `/leaderboard/daily`
- **Method**: `GET`
- **Response Payload (`200 OK`)**:
  ```json
  {
    "status": "SUCCESS",
    "code": 200,
    "message": "Request processed successfully",
    "data": [
      {
        "userId": 1,
        "name": "Jane Doe",
        "avatarUrl": "https://cloudinary.com/avatar.png",
        "value": 50,
        "rank": 1
      },
      {
        "userId": 2,
        "name": "John Smith",
        "avatarUrl": null,
        "value": 25,
        "rank": 2
      }
    ]
  }
  ```

### Get Activity Feed
Retrieves the 20 most recent completed focus sessions by users that the current user is following.
- **URL**: `/feed`
- **Method**: `GET`
- **Response Payload (`200 OK`)**:
  ```json
  {
    "status": "SUCCESS",
    "code": 200,
    "message": "Request processed successfully",
    "data": [
      {
        "id": 500,
        "userId": 2,
        "userName": "John Smith",
        "userAvatarUrl": null,
        "type": "WORK_SESSION",
        "timestamp": "2026-06-07T12:00:00Z",
        "content": "đã hoàn thành 25 phút tập trung"
      }
    ]
  }
  ```

---

## 2. Public Profiles & Search

### Search Users
Finds users by name or email.
- **URL**: `/users/search`
- **Method**: `GET`
- **Query Parameter**: `q` (search query string)
- **Response Payload (`200 OK`)**:
  ```json
  {
    "status": "SUCCESS",
    "code": 200,
    "message": "Request processed successfully",
    "data": [
      {
        "userId": 2,
        "name": "John Smith",
        "avatarUrl": null,
        "isFollowing": true
      }
    ]
  }
  ```

### Get Public Profile
Fetves streaks, badges, and focus stats for any user profile.
- **URL**: `/users/{targetUserId}/profile`
- **Method**: `GET`
- **Response Payload (`200 OK`)**:
  ```json
  {
    "status": "SUCCESS",
    "code": 200,
    "message": "Request processed successfully",
    "data": {
      "userId": 2,
      "name": "John Smith",
      "avatarUrl": null,
      "isFollowing": true,
      "currentStreak": 5,
      "longestStreak": 12,
      "totalActiveDays": 18,
      "totalWorkMinutes": 1200,
      "badges": [
        {
          "code": "STREAK_5",
          "name": "Five-Day Burn",
          "icon": "🔥",
          "rarity": "COMMON"
        }
      ]
    }
  }
  ```

---

## 3. Shared Goals Management

### Enable Shared Goal
Converts a private goal into a collaborative shared goal. Adds the owner as `OWNER` member.
- **URL**: `/goals/{goalId}/share`
- **Method**: `POST`
- **Response**: `204 No Content`

### Invite Friend to Goal
Sends a pending invitation to join the shared goal. The invitee must be a mutual friend.
- **URL**: `/goals/{goalId}/invitations`
- **Method**: `POST`
- **Request Payload (`InviteMemberRequest`)**:
  ```json
  {
    "inviteeId": 2
  }
  ```
- **Response**: `204 No Content`

### Cancel Invitation (Owner)
Cancels a pending invitation.
- **URL**: `/goals/{goalId}/invitations/{invitationId}`
- **Method**: `DELETE`
- **Response**: `204 No Content`

### Kick Member (Owner)
Removes a member from the goal.
- **URL**: `/goals/{goalId}/members/{memberId}`
- **Method**: `DELETE`
- **Response**: `204 No Content`

### Leave Shared Goal (Member)
Allows a non-owner member to leave a shared goal.
- **URL**: `/goals/{goalId}/members/me`
- **Method**: `DELETE`
- **Response**: `204 No Content`

### Respond to Goal Invitation (Invitee)
Accepts or declines a pending invitation in the user's inbox.
- **URL**: `/invitations/{invitationId}`
- **Method**: `PATCH`
- **Request Payload (`RespondInvitationRequest`)**:
  ```json
  {
    "accept": true
  }
  ```
- **Response**: `204 No Content`

### View My Inbox Invitations
Fetches all pending shared goal invitations sent to the current user.
- **URL**: `/invitations`
- **Method**: `GET`
- **Response Payload (`200 OK`)**:
  ```json
  {
    "status": "SUCCESS",
    "code": 200,
    "message": "Request processed successfully",
    "data": [
      {
        "id": 50,
        "goalId": 1,
        "goalTitle": "Build Portfolio App",
        "inviterId": 1,
        "inviterName": "Jane Doe",
        "inviterAvatarUrl": "https://cloudinary.com/avatar.png",
        "inviteeId": 2,
        "inviteeName": "John Smith",
        "status": "PENDING",
        "createdAt": "2026-06-07T12:00:00Z",
        "respondedAt": null
      }
    ]
  }
  ```

### View Member Progress Board
Retrieves the list of members and their relative contributions towards the shared goal target.
- **URL**: `/goals/{goalId}/members`
- **Method**: `GET`
- **Response Payload (`200 OK`)**:
  ```json
  {
    "status": "SUCCESS",
    "code": 200,
    "message": "Request processed successfully",
    "data": [
      {
        "userId": 1,
        "name": "Jane Doe",
        "avatarUrl": "https://cloudinary.com/avatar.png",
        "role": "OWNER",
        "joinedAt": "2026-06-07T05:00:00Z",
        "todayMinutes": 50,
        "totalMinutes": 120,
        "progress": 20
      },
      {
        "userId": 2,
        "name": "John Smith",
        "avatarUrl": null,
        "role": "MEMBER",
        "joinedAt": "2026-06-07T12:10:00Z",
        "todayMinutes": 25,
        "totalMinutes": 25,
        "progress": 4
      }
    ]
  }
  ```
*Note*: `progress` is computed dynamically as a percentage (e.g. `20%`) relative to the goal's `targetTotalMinutes`.
