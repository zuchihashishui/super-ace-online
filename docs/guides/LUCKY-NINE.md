# Lucky 9 — V25

An independent single-player house variant. Every hand uses a fresh, uniformly shuffled 52-card deck. Player and banker start with two cards. A is one point, 2–9 are face value, 10/J/Q/K are zero; the sum modulo ten is the score. An initial 8 or 9 on either side settles both two-card hands immediately. Otherwise the player chooses one additional card or stands. Banker draws on 0–5 and stands on 6–9. The higher score wins; ties refund the stake. Gross returns are 1.95× for a win, 1× for a tie, zero for a loss, rounded down to cents. Super Ace RTP controls do not affect this game.

Basic card values and draw/stand concept reference: https://play.google.com/store/apps/details?hl=en_US&id=com.luckynine.pokergame
The natural rule, banker threshold and paytable above define this implementation explicitly; they are not claimed to reproduce every app's rules.

Only the player's two cards are returned while a hand is active. The full shuffled deck remains in a private database table. A completed response contains only the cards actually dealt, never the remaining deck. Player and wallet scope are enforced on reads and moves. Wallet row locking prevents concurrent duplicate starts and decisions. Reusing a settled decision with a different action is rejected. Reloading or restarting preserves the open hand and accepted request ID.

Use the new Flyway V19 migration for an existing installation. `database/super_ace.sql` is a fresh-install script; do not re-import it into existing data. Startup applies V19 automatically; manual alternatives are `database/upgrade_v18_to_v19.sql` and the matching script for older schemas. Existing credentials, wallets, receipts and mine boards are preserved.

Java game code: `philip.emerald.ace.luckynine`; shared deck shuffling: `philip.emerald.ace.Utils.Decks`.

Verification: 51 targeted backend tests passed (including shared server regressions, hand rules and V18→V19 migration). Actual packaged-JAR Chromium checks passed at 320/390/760/844/1440px: hidden banker, hit/stand, active-hand reload, lost accepted decision recovery exactly once, ties, visible win effects, fixed mobile controls, account registration, wallet isolation, sound and Filipino. Tested with Java 21 and disposable H2 MySQL mode, not a live MySQL server or physical phones.
