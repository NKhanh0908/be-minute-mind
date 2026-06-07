# Business Domain: The Productivity Ecosystem

## Introduction

At its core, **MinuteMind** is not just another Pomodoro app or simple task manager. It is a **Gamified Productivity Ecosystem** designed to tackle a universal human problem: **the gap between long-term goals and daily execution**.

Most productivity tools fail because they are disjointed. Task managers are passive lists of chores. Timers are isolated clocks that don't connect to goals. Social networks are distractions rather than motivators. 

MinuteMind solves this by creating a closed, self-reinforcing loop where **long-term goals are directly fueled by daily focus, validated by real-time tracking, rewarded by gamification, and sustained by social accountability**.

---

## The Core Value Loop: Goal to Social Accountability

The system operates as a unified chain of behaviors, where each step feeds into the next:

```
[Goal] ➔ [Task] ➔ [Focus Session] ➔ [Streak] ➔ [Badge] ➔ [Leaderboard] ➔ [Community Accountability]
```

### 1. Goal (The Vision)
A **Goal** represents the macro objective (e.g., "Build a Personal Portfolio", "Learn Advanced Spring Boot"). It provides direction and purpose. Without a goal, daily tasks lack context and meaning. In MinuteMind, goals define:
- Target focus minutes (e.g., 2,000 minutes).
- Deadlines.
- Custom colors to visually group related activities.
- Shareability status (Personal vs. Shared).

### 2. Task (The Action Plan)
A goal is too abstract to act on directly. Therefore, it must be decomposed into concrete, actionable **Tasks** (e.g., "Design Database Schema", "Set up Security Filters"). Tasks contain:
- Estimated focus minutes.
- Current status (`TODO`, `IN_PROGRESS`, `DONE`).
- Dynamic logging: Every minute spent in a focus session on a task is automatically aggregated under both the task and its parent goal.

### 3. Focus Session (The Execution)
This is where the actual work happens. A **Focus Session** (`WorkSession`) binds a user to a specific task using a planned timer (defaulting to the classic 25-minute Pomodoro, but fully customizable). 
- To prevent cheating, sessions are tracked in real-time using a **Heartbeat** mechanism.
- Redis is utilized as an active session cache to ensure a user cannot run concurrent sessions, enforcing single-tasking discipline.
- Focus sessions are categorized as either `WORK` or `BREAK`, but only active `WORK` minutes count toward productivity progression.

### 4. Streak (The Momentum Builder)
Productivity is a function of consistency. The system tracks a user's daily **Streak** to build behavioral momentum.
- A user must meet their self-defined **Streak Threshold** (e.g., 25 focus minutes in a single day) for that day's activity to be considered valid.
- Once the threshold is crossed, the daily streak increments.
- Missing a day resets the current streak, encouraging the user to maintain the habit loop.

### 5. Badge (The Instant Gratification)
To provide positive reinforcement, the system awards **Achievement Badges**. 
- Immediately upon completing a focus session, the backend checks if the user qualifies for new badges.
- Badges are awarded based on two main metrics: **Total Focus Minutes** (cummulative output) or **Consecutive Streak Days** (consistency).
- This introduces a gamification element, transforming effort into a tangible collection of achievements.

### 6. Leaderboard (Friendly Competition)
Human beings are social creatures who are motivated by peer comparison. The **Daily Focus Leaderboard** aggregates the daily focus minutes of the user and their followed friends.
- It resets daily, giving everyone a fresh start each morning.
- It creates a micro-community of friendly competition, where users can see their standing climb in real-time as they complete focus sessions.

### 7. Community Accountability (Shared Execution)
Social accountability is the ultimate deterrent to procrastination. MinuteMind introduces two social layers:
- **Activity Feed**: A transparent timeline showing when friends complete focus sessions, celebrating their dedication.
- **Shared Goals**: The ability for mutual friends (up to 10 members) to collaborate on a single goal. Members can see each other's today's focus minutes, total goal contributions, and progress percentages. If one member slacks off, the entire group's target is delayed, leveraging positive peer pressure.

---

## Why this Architecture Exists

Every database table and API endpoint in this project exists to support this psychological loop. 
- The **Redis Active Session Cache** guarantees focus integrity (prevents false logging).
- The **Soft Delete** mechanism on Goals and Tasks preserves history for stats and streak activities, ensuring past efforts are never lost.
- The **Mutual-Follow constraint** for shared goal invitations ensures that shared goals remain intimate and supportive circles rather than spam targets.

By transforming raw time-tracking into social reputation and gamified status, MinuteMind converts passive screen-time into active, focused execution.
