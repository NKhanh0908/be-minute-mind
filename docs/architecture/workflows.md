# System Workflows & Flowcharts

This document serves as an index for the major business and technical workflows in the MinuteMind backend. 

---

## Detailed Workflows

To make the system architecture easy to understand, we have mapped out the core lifecycles using Mermaid flowcharts. Click the links below to view the visual diagrams and detailed descriptions for each flow:

### 1. [Focus Session Lifecycle (WorkSession Flow)](../diagrams/session-flow.md)
Maps the step-by-step API interaction and database updates during a focus session:
- Starting a timer, validating active session status, locking concurrent sessions using Redis.
- Handling continuous Heartbeats and updating actual focus duration.
- Completing or discarding the session, auto-updating task states, and triggering gamification checks.

### 2. [Shared Goals & Invitation Lifecycle](../diagrams/shared-goal-flow.md)
Visualizes how users collaborate on Shared Goals:
- Enabling shared mode for a goal and registering roles (`OWNER` vs `MEMBER`).
- Restricting goal invitations to mutual follow relationships and enforcing member limits (maximum of 10).
- Handling invite acceptances, declines, leaves, and kicks.

### 3. [Gamification & Badge Issuing Flow](../diagrams/badge-flow.md)
Outlines the logic behind passive reward systems:
- Accumulating daily focus minutes against a user-configured threshold.
- Evaluating consecutives days to increment or reset streak counters.
- Scanning available badges, evaluating condition types (`TOTAL_MINUTES` and `STREAK_DAYS`), and issuing `UserBadge` records.
