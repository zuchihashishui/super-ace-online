# Editable bet
- Default/minimum 5; maximum 500; increments of 0.01 in both Lobby and Club.
- +/- adjusts by 5. Input is locked during a pending spin, autoplay or free spins.
- Engine, manual-spin and autoplay validation share the same integer-cent range.
- No database migration required.
- RTP is shared by all accounts within its mode; bonus sessions retain their starting profile.
- Verified packaged Java 21 release with disposable H2: custom wagers, boundaries, retry idempotency, autoplay start/stop, wallet isolation, auth and DOM controls. Native Windows/MySQL not run here.
