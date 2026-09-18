# V40: Color Game adaptive graphics

## Controls
Open the in-game hamburger menu, then Graphics quality:
- Auto (default): starts at the V38 Full HD density target. After at least 30 measured frames, a round averaging over 23 ms reduces density by 0.15 down to 0.5 of the target. Three fast rounds averaging under 18 ms restore 0.1. Adjustment occurs after the roll, keeping the physics unchanged.
- High: fixed Full HD density target, bounded by the existing 3M pixel budget and hardware texture limit.
- Battery Saver: caps density at 1.25, disables realtime dice shadows, chip flights and victory particles; rays remain static.
Preference persists in localStorage. This is a rendering setting, not a rule or RTP setting.

## Changes
Warm key light plus cool rim light give the rounded dice depth. Reused interpolation vectors and quaternions reduce allocations during animation. Renderer and physics worker initialize on game entry. Existing idle rendering stop is preserved and now tested. Impact audio starts at the impact cue instead of adding another long delay.
Accepted bets produce bounded decorative chip flights (maximum 12), with coordinates matching the rotated phone board. Positive net returns send decorative chips from winning staked colors to the wallet once per owner/round. Flights do not change balances. Winning tiles have a brighter inset highlight. Reduced-motion disables flights.

## Validation
Tests serve assets extracted from the actual release JAR:
- Real WebGL and physics on five viewport/DPR configurations; outcome colors, movement, resize, cancellation, context-loss fallback.
- Battery density limit, saved High preference, idle render count, Auto reduction under deliberately delayed frames.
- Six fullscreen/navigation cases, menu controls, chip-flight cleanup and Battery suppression.
- Ten phone win/receipt configurations (normal and reduced motion), plus desktop and rotation during a win.
These are Chromium fixtures, not physical-phone FPS/battery measurements or live MySQL wallet integration tests. Backend code and schema unchanged.

## Install
Complete source plus refreshed release JAR. Run start.bat and reload the browser. No SQL update.
