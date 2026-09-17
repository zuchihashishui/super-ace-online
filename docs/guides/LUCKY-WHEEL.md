# Lucky Wheel — V21 application / V17 database

A new independent virtual-chip wheel, with 16 equal sectors and uniform selection by SecureRandom. Clockwise gross multipliers: 0, 1, 0, 0.5, 0, 2, 0, 0.5, 0, 1, 0, 3, 0, 0.5, 0, 7. Every sector has probability 1/16. Gross return includes stake; zero pays zero. Returns round down to cents. Theoretical whole-chip RTP: 15.5/16 = 96.875%. These explicit project rules do not claim to reproduce a third-party app; Super Ace settings do not change them.

The server commits the outcome and wallet together, and the canvas eases through five turns to the selected sector under the fixed top pointer. Visual rotation cannot choose or alter the outcome. Bet size is 5–500. One spin at a time, with responsive layouts, mute/reduced-motion support, English/Filipino and 20-bet history. WHEEL and PLINKO receipts use distinct game types. A request ID cannot be reused between games. Lobby and Club stay isolated.

No new database migration beyond V17 is required. Keep the existing database when updating from V19/V20. Earlier versions apply V17 automatically through Flyway. Never import the fresh-install SQL into a populated database.

Backend verification covers every sector, payout totals, separate histories, cross-game replay rejection and concurrent Plinko retry regression. Packaged-JAR browser verification covers responsive widths, guest/registration, lost accepted response plus reload, displayed result, history limit, empty Club wallet, Filipino and sound. Environment is Chromium with H2 MySQL compatibility; native Windows and live MySQL were not exercised.
