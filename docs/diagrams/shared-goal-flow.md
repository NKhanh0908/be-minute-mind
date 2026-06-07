# Shared Goals & Invitation Workflow

This diagram models the lifecycle of shared goals, friend invitations, and membership state transitions.

```mermaid
flowchart TD
    Start([Goal Owner]) --> Share[POST /community/goals/{id}/share]
    Share --> SetShared[Set goal.isShared = true]
    SetShared --> JoinOwner[Add Owner to goal_members as 'OWNER']
    JoinOwner --> Invite[POST /community/goals/{id}/invitations]
    
    Invite --> ValOwner{Is Caller OWNER?}
    ValOwner -- No --> Deny[Error: Unauthorized]
    ValOwner -- Yes --> ValFriend{Do they mutually follow each other?}
    
    ValFriend -- No --> ErrorFriend[Error: Can only invite mutual friends]
    ValFriend -- Yes --> ValMember{Already a Member or Pending?}
    
    ValMember -- Yes --> ErrorMem[Error: Already member or invite pending]
    ValMember -- No --> ValLimit{Current members + Pending < 10?}
    
    ValLimit -- No --> ErrorLimit[Error: Goal is full, limit is 10 members]
    ValLimit -- Yes --> CreateInvite[Create GoalInvitation as PENDING]
    
    CreateInvite --> SendNotif[Create Notification for Invitee with payload]
    SendNotif --> InviteeInbox([Invitee receives invitation in inbox])
    
    InviteeInbox --> Respond[PATCH /community/invitations/{invitationId}]
    
    Respond --> Action{Accept or Decline?}
    
    Action -- Decline --> SetDeclined[Set status = DECLINED, set respondedAt = now]
    SetDeclined --> EndDecline([Invitation Declined])
    
    Action -- Accept --> ReValLimit{Is member count < 10?}
    ReValLimit -- No --> AutoDecline[Set status = DECLINED, throw Exception: Goal Full]
    ReValLimit -- Yes --> SetAccepted[Set status = ACCEPTED, set respondedAt = now]
    SetAccepted --> CreateMember[Add User to goal_members as 'MEMBER']
    CreateMember --> ActiveMember([User is now Goal Member])
    
    ActiveMember -- User leaves --> Leave[DELETE /community/goals/{id}/members/me]
    Leave --> CheckOwner{Is OWNER?}
    CheckOwner -- Yes --> ErrLeave[Error: Owner cannot leave, must Archive goal]
    CheckOwner -- No --> DelMember[Remove from goal_members]
    
    ActiveMember -- Owner kicks --> Kick[DELETE /community/goals/{id}/members/{memberId}]
    Kick --> CheckKickOwner{Is member OWNER?}
    CheckKickOwner -- Yes --> ErrKick[Error: Cannot kick OWNER]
    CheckKickOwner -- No --> DelMember
    
    DelMember --> Left([User removed from Goal])
```
