# Plinko — V20 application / V17 database

12 independent fair left/right choices produce one of 4096 equally likely paths. The server returns the exact path, slot, multiplier and settled receipt; the canvas follows that path. The number of right turns is the final slot (0–12).

Gross multipliers from left to right: 50, 10, 3, 1.5, 1, 0.7, 0.5, 0.7, 1, 1.5, 3, 10, 50. Gross returns include the stake and round down to cents. Bin probabilities are C(12,k)/4096; multipliers must not be averaged without weighting those probabilities. Theoretical return for whole-chip stakes is 96.6015625%, slightly lower for fractional stakes due to payout rounding. Super Ace RTP does not affect this game. This is an explicit project paytable, not a reproduction of another provider's undisclosed rules.

Bets are 5–500. Guests can inspect the board and rules. Registered playable roles can drop one ball at a time. Lobby and Club wallets, personal history and pending request keys are separate. Existing ledgers store PLINKO receipts, so no migration beyond V17 is needed. Club reporting includes these wagers and payouts. Reload/retry preserves the original UUID and cannot charge twice.

Verification: all 4096 paths, payout rounding and concurrent identical requests tested; packaged-JAR browser suite passed at 320/390/760/844/1440 widths, including guest/registration, lost accepted response and reload, displayed slot/payout, 20-bet history limit, language, sound and empty Club balance. Test environment is Java 21, Chromium, H2 MySQL compatibility, not native Windows or live MySQL.
