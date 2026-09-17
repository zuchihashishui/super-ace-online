# V28 — Plinko animation reliability and performance

- Reset the old ball and highlighted bin as soon as a drop starts. Show “Receiving result…” while awaiting the server so an old landed ball is not mistaken for a stuck new drop.
- Cache the static 1040×840 Plinko board once. The animation reuses it instead of redrawing 78 glowing pegs and the background each frame.
- Drive animation from consecutive requestAnimationFrame deltas, capped at 40 ms. A stalled frame cannot consume the whole animation or skip most of the route. Plinko now runs for 2.6 seconds of rendered progress with accelerated arcs between peg contacts.
- Complete background/reduced-motion animations by drawing the authoritative final landing before resolving. Previously the background handler resolved without drawing the final frame.
- Catch drawing failures and release the animation promise instead of leaving controls permanently busy. Preserve the existing pending request for safe retry.
- Server outcomes, route, payout, wallet rules and database schema are unchanged. Keep the existing database; do not re-import SQL.

Validation is recorded below after tests against the packaged Java 21 release JAR with isolated H2 in MySQL compatibility mode. This is not a benchmark on physical phones or a test against the user's MySQL.

All four browser suites passed against the packaged release: plinko-motion, plinko, wheel and slots. Motion checks cover three consecutive visible drops, intermediate/final frames, one cached board, delayed HTTP response, disabled controls while pending, background completion and reduced motion. Regression checks cover five responsive widths, accepted response loss/reload with exactly-once debit, route/slot/payout agreement, 20-round history, wallet isolation, language and audio. Java backend code was not changed; backend unit tests were not rerun for this UI-only update.
