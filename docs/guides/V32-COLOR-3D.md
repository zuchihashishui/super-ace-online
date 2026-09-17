# V32 — Physical 3D Color Game dice and fiesta graphics

Color Game uses a locally bundled Three.js WebGL renderer and cannon-es rigid-body simulation. Each die has a rounded ivory shell and six distinct colored faces with symbols. Three dynamic box colliders fall under gravity, interact with the floor, walls and each other, and settle through friction/damping. Lighting and floor shadows make cube depth and contact visible.

Physics is precomputed in a dedicated Web Worker at 120 Hz, recording poses at 60 Hz. The server's three colors remain authoritative: after simulation identifies each final upward face, its six materials are assigned before the first rendered frame. Materials stay fixed during playback; there is no late texture swap or payout derived from browser physics. Failed/non-flat simulations retry up to three times and fall back to the existing CSS presentation if necessary. Rendering interpolates recorded positions and quaternions over three seconds and stops when settled.

The renderer caps device pixel ratio at 1.5, uses a 512-pixel shadow map and a single canvas. Reduced-motion/background completion, cancellation on reset, worker timeout and WebGL-loss fallback preserve the game flow. The fallback is the earlier CSS cube animation; a browser without usable WebGL cannot display the new 3D renderer.

The interface adapts the supplied reference: pink/orange case, wooden six-color board, purple/gold ranking panel and an original SVG festival park background. Player names, amounts and rankings remain real server data. The pink marquee displays the existing 4× return and timer; no fictional jackpot or new side bets were added. Existing betting rules and database schema are unchanged.

## Build and run

- All assets are served locally; players need no CDN connection.
- Rebuild renderer: `cd tools/dice-renderer`, `npm ci`, `npm run build`.
- Dependencies pinned in package-lock.json: Three.js 0.170.0, cannon-es 0.20.0, esbuild 0.24.2. Runtime licenses are included in client/vendor.
- Package normal server release after client build, or use tools/build_release.py with compiled server classes.
- Client tag: `38-webgl`. Stop the old server, run the new package, reload with Ctrl+F5. Keep the existing database/configuration; do not re-import SQL.

References: https://threejs.org/docs/ and https://pmndrs.github.io/cannon-es/.

Validation uses the actual Java 21 release JAR and isolated H2. WebGL is tested through Chromium software rendering, including real physics poses, collisions, stable top faces, all six colors/repeated colors, cancellation, GPU-loss fallback and responsive widths. This does not certify performance on every physical phone or test the user's MySQL database. No backend code or payout changes.

All three browser suites passed: color3d, color27 (payout/limits/mapping) and color (full game regression). After the final context-loss handling change, color3d passed again against the rebuilt release, now calculating the displayed top color from each mesh's actual quaternion/material rather than a stored outcome label. Includes context loss during worker computation; the canceled result does not reappear over the fallback. All six colors and repeated-color outcomes passed. Screenshot review at 320px found and corrected horizontal camera clipping and ranking-title overflow. Existing full-game checks cover guest/register, chip stacks, exact payouts, request retries/reload, history, Filipino, audio, two-player totals and wallet isolation.
