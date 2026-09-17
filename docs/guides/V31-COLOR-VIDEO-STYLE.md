# V31 — Color Game presentation inspired by the supplied video

Reviewed the 16.39-second reference clip as full-size frames and a quarter-second motion sequence around the dice drop. Adapted the orange hinged tray, upper feeder compartment, purple/gold frame, vivid six-color board and longer tumbling/bouncing dice sequence. The landscape layout keeps the ranking/tray/betting-board arrangement; narrow portrait screens stack the panels with usable touch targets.

Dice now travel from the upper compartment into the lower tray over 2.1 seconds, staggered by 65 ms. The first floor contact occurs at 40% of the motion, followed by decreasing bounce heights. The tray allows the dice to cross its upper boundary, avoiding clipping the drop. Static and animated final orientations agree; the upper face remains the server-selected color, RED star and PINK heart. Reduced motion and background completion remain supported.

This is an adaptation using the project's own HTML/CSS/3D cubes, not a pixel-identical copy or extracted artwork. The reference includes pair/triple side bets, a jackpot and different payout labels. Those were not added: existing six-color wagers, maximum three selected colors and the 2×/3×/4× gross payout rules remain unchanged. No simulated player bets or jackpot values were added.

No database changes. Client cache tag: 37-fiesta. Stop the old server, run the new release and reload with Ctrl+F5. Keep the existing database and configuration.

Validation: both Color Game browser suites passed against the packaged Java 21 release using isolated H2. Includes six-color top-face mapping, exact payout example, three-color cap, top-ups, live animation, five viewport widths including landscape, guest/register, chip stacks, retries/reload, history, Filipino, audio, multiplayer totals and wallet isolation. Desktop and phone screenshots visually inspected. No physical-phone or user-MySQL benchmark; backend code unchanged.
