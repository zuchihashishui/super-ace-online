# Super Ace — Scatter awards 10 Free Spins

- Three or more Scatter symbols on the initial 5 × 4 board award 10 Free Spins.
- The trigger rule is unchanged: only the initial board triggers; Scatter after a cascade does not.
- Scatter during a Free Spin can award another 10 Free Spins.
- The stake remains locked until all awarded Free Spins are consumed.
- The server remains authoritative; matching client labels and fallback preview logic now also show 10.
- No database migration is required.

## RTP calibration

The Scatter probability per generated symbol is 3/124. For 20 independent initial cells, the probability of at least three Scatters is 0.011862585340885842. Recursive 10-spin awards increase the expected number of evaluated rounds, so the unscaled cycle baseline changes from 0.96712357778 to 0.99315698487. `GameEngine.scaleFor` now uses the new baseline, keeping configured Super Ace RTP targets (including Lobby 100% and Club 97.5%) consistent in long-run expectation.

Already-started Free Spins keep their stored RTP profile, while each new Scatter trigger awards 10 spins.
