# Java package map

Root application: `philip.emerald.ace.AceApplication`.

| Package under `philip.emerald.ace` | Responsibility |
|---|---|
| `superace` | Super Ace outcomes, free spins, autoplay and its RTP settings |
| `dragontiger` | Dragon Tiger rules and shared timed table |
| `colorgame` | Three-color-dice rules and shared timed table |
| `crash` | Shared Crash rounds and server cashout |
| `plinko` | Plinko path and paytable |
| `luckywheel` | Lucky Wheel sectors and paytable |
| `slots` | Three-reel Slots rules |
| `mines` | Secret mine boards, moves and cashout |
| `sakla` | Spanish-card pairs and first completed pair |
| `luckynine` | Private card deals and hit/stand settlement |
| `bingo` | 75-ball cards, draws and line matching |
| `Utils` | Wallet storage, account hierarchy, JWT, controllers, reports, shared deck shuffle and transaction dispatch for immediate ticket games |

`Utils` uses the capitalization requested for this project. New package paths are included in the release JAR and its Start-Class manifest. All previously released Flyway migrations remain unchanged. JSON receipts store records rather than Java class names, so old histories do not need rewriting.

Build normally with Java 21 JDK and Maven: `mvn -f server/pom.xml clean verify`.
Run the packaged browser suite using `python tools/run_browser_checks.py`; see V26-VERIFICATION for dependencies. `tools/build_release.py` is an offline packaging fallback and verifies dependency archive integrity and the Java entry point layout.
