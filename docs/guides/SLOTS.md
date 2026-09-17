# Seven Slots — V22 application / V17 database

A separate three-reel, single-center-line game. Each reel independently draws one of ten equally likely stops: four cherries, three lemons, two bells and one seven. Three cherries pay 5× gross; three lemons 10×; three bells 25×; three sevens 100×. Exactly two sevens anywhere on that line pay 3×. Everything else pays zero. Gross return includes stake. No bonus rounds, wilds, scatter or progressive jackpot are implied. Super Ace is unchanged and its RTP settings do not apply here.

Expected gross return is 0.4³×5 + 0.3³×10 + 0.2³×25 + 0.1³×100 + 3×0.1²×0.9×3 = 97.1%. These are this project's explicit rules, not a copy of an undisclosed commercial math model.

The server commits one authoritative result with wallet revision and request ID, then the UI animates to those three symbols. Repeated identical requests return the same receipt; game/payload reuse is rejected. Stakes are 5–500; wallets and histories remain separate by currency/game. No additional migration beyond V17.

Every one of 1,000 stop combinations was tested against the paytable and total return. Packaged-JAR browser checks cover guest/registration, five responsive sizes, a lost accepted response followed by reload, exact symbol/payout matching, history capped at 20, language, sound and Club isolation. Test runtime is Java 21 / H2 MySQL compatibility / Chromium; actual MySQL, Windows and physical phones were not available.
